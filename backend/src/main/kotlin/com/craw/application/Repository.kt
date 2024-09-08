package com.craw.application

import com.craw.schema.database.CrawlRelationsTable
import com.craw.schema.database.CrawlsTable
import com.craw.schema.database.ExecutionsTable
import com.craw.schema.database.RecordEntity
import com.craw.schema.database.RecordsTable
import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordDelete
import com.craw.schema.internal.RecordState
import com.craw.schema.internal.RecordUpdate
import com.craw.translator.DatabaseTranslator
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

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
    fun delete(recordDelete: RecordDelete): Boolean = transaction(database) {
        val record = RecordEntity.findById(recordDelete.recordId.toUUID()) ?: return@transaction false
        val executions = record.executions
        executions.flatMap { it.crawls }.forEach { it.delete() }
        executions.forEach { it.delete() }
        record.delete()
        true
    }

    fun create(record: RecordCreate): String = transaction(database) {
        val new = RecordEntity.new {
            url = record.baseUrl
            regexp = record.regexp
            periodicity = record.periodicity
            label = record.label
            tags = record.tags
            active = record.active
        }
        new.id.value.toString()
    }

    fun update(record: RecordUpdate): Boolean = transaction(database) {
        val found = RecordEntity.findByIdAndUpdate(record.recordId.toUUID()) {
            it.url = record.baseUrl
            it.regexp = record.regexp
            it.periodicity = record.periodicity
            it.label = record.label
            it.active = record.active
            it.tags = record.tags
        }
        found != null
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}
