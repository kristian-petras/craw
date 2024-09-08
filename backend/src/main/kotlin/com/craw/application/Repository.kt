package com.craw.application

import DatabaseFactory
import com.craw.schema.database.CrawlEntity
import com.craw.schema.database.CrawlRelationsTable
import com.craw.schema.database.CrawlType
import com.craw.schema.database.CrawlsTable
import com.craw.schema.database.ExecutionEntity
import com.craw.schema.database.ExecutionType
import com.craw.schema.database.ExecutionsTable
import com.craw.schema.database.RecordEntity
import com.craw.schema.database.RecordsTable
import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordState
import com.craw.schema.internal.RecordUpdate
import com.craw.translator.DatabaseTranslator
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun main() {
    val database = DatabaseFactory.postgres(dotenv()["POSTGRES_PASSWORD"])

    val id = transaction(database) {
        SchemaUtils.drop(RecordsTable, ExecutionsTable, CrawlsTable, CrawlRelationsTable)
        SchemaUtils.createMissingTablesAndColumns(RecordsTable, ExecutionsTable, CrawlsTable, CrawlRelationsTable)

        val record = RecordEntity.new {
            name = "Hello"
            url = "https://example.com"
            regexp = ".*"
            periodicity = "daily"
            label = "Example"
            tags = listOf("example", "test")
            active = true
        }

        val execution = ExecutionEntity.new {
            url = "https://333.com"
            regexp = ".*"
            start = java.time.Instant.now()
            end = java.time.Instant.now()
            this.record = record
            type = ExecutionType.COMPLETED
        }

        val crawl = CrawlEntity.new {
            url = "https://111.com"
            title = "Example"
            start = java.time.Instant.now()
            end = java.time.Instant.now()
            this.execution = execution
            type = CrawlType.COMPLETED
        }

        val children = CrawlEntity.new {
            url = "https://222.com"
            title = "Example"
            start = java.time.Instant.now()
            end = java.time.Instant.now()
            this.execution = execution
            type = CrawlType.COMPLETED
        }

        children.parents = SizedCollection(listOf(crawl))

        record.id.value.toString()
    }

    val repo = Repository(DatabaseTranslator(), database)
    repo.delete(recordId = id)
}

class Repository(private val translator: DatabaseTranslator, private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(RecordsTable, ExecutionsTable, CrawlsTable, CrawlRelationsTable)
        }
    }

    fun records(): List<RecordState> {
        val records = transaction(database) {
            RecordEntity.all()
        }
        return records.map { translator.translate(it) }
    }

    fun record(id: String): RecordState? {
        val record = transaction(database) {
            RecordEntity.findById(id.toUUID())
        }
        return record?.let { translator.translate(it) }
    }

    /**
     * Deletes a record and all associated executions and crawls.
     */
    fun delete(recordId: String): Boolean = transaction(database) {
        val record = RecordEntity.findById(recordId.toUUID()) ?: return@transaction false
        val executions = record.executions
        executions.flatMap { it.crawls }.forEach { it.delete() }
        executions.forEach { it.delete() }
        record.delete()
        true
    }

    fun create(record: RecordCreate): String {
        TODO()
    }

    fun update(record: RecordUpdate): Boolean {
        TODO()
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}
