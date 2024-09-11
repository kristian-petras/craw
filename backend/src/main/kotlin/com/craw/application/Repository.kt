package com.craw.application

import com.craw.schema.database.CrawlEntity
import com.craw.schema.database.CrawlRelationsTable
import com.craw.schema.database.CrawlType
import com.craw.schema.database.CrawlsTable
import com.craw.schema.database.ExecutionEntity
import com.craw.schema.database.ExecutionType
import com.craw.schema.database.ExecutionsTable
import com.craw.schema.database.RecordEntity
import com.craw.schema.database.RecordsTable
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordDelete
import com.craw.schema.internal.RecordState
import com.craw.schema.internal.RecordUpdate
import com.craw.translator.DatabaseTranslator
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
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

    fun invalidate(record: RecordUpdate): Boolean = transaction(database) {
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

    fun create(executionCreate: ExecutionCreate): Execution.Scheduled = transaction(database) {
        val record = RecordEntity.findById(executionCreate.recordId.toUUID())
            ?: error("Record ${executionCreate.recordId} not found while creating new execution")

        val new = ExecutionEntity.new {
            url = executionCreate.baseUrl
            regexp = executionCreate.regexp
            start = executionCreate.start.toJavaInstant()
            type = ExecutionType.SCHEDULED
            this.record = record
        }

        translator.translate(new) as Execution.Scheduled
    }

    fun invalidate(executionDelete: ExecutionInvalidate): Execution.Removed = transaction(database) {
        val execution = ExecutionEntity.findByIdAndUpdate(executionDelete.executionId.toUUID()) {
            it.type = ExecutionType.INVALID
            it.end = executionDelete.end.toJavaInstant()
        } ?: error("Execution ${executionDelete.executionId} not found while invalidating")

        translator.translate(execution) as Execution.Removed
    }

    fun start(executionStart: ExecutionStart): Execution.Running = transaction(database) {
        val execution = ExecutionEntity.findByIdAndUpdate(executionStart.executionId.toUUID()) {
            it.type = ExecutionType.RUNNING
        } ?: error("Execution ${executionStart.executionId} not found while starting")

        translator.translate(execution) as Execution.Running
    }

    fun complete(executionComplete: ExecutionComplete): Execution.Completed = transaction(database) {
        val execution = ExecutionEntity.findByIdAndUpdate(executionComplete.executionId.toUUID()) {
            it.type = ExecutionType.COMPLETED
            it.end = executionComplete.end.toJavaInstant()
        } ?: error("Execution ${executionComplete.executionId} not found while completing")

        val crawl = CrawlEntity.findByIdAndUpdate(executionComplete.crawlId.toUUID()) {
            it.type = CrawlType.COMPLETED
            it.end = executionComplete.end.toJavaInstant()
        } ?: error("Execution ${executionComplete.executionId} should have a single completed crawl but does not")

        translator.translate(execution, crawl)
    }

    fun create(crawlCreate: CrawlCreate): Crawl.Pending = transaction(database) {
        val execution = ExecutionEntity.findById(crawlCreate.executionId.toUUID())
            ?: error("Execution ${crawlCreate.executionId} not found while creating new crawl")

        val new = CrawlEntity.new {
            url = crawlCreate.url
            type = CrawlType.PENDING
            this.execution = execution
        }
        if (crawlCreate.parentId != null) {
            val parent = CrawlEntity.findById(crawlCreate.parentId.toUUID())
                ?: error("Parent ${crawlCreate.parentId} not found while creating new crawl")

            new.parent = SizedCollection(parent)
        }
        translator.translate(new) as Crawl.Pending
    }

    fun invalidate(crawlNotMatched: CrawlNotMatched): Crawl.Invalid = transaction(database) {
        val execution = ExecutionEntity.findById(crawlNotMatched.executionId.toUUID())
            ?: error("Execution ${crawlNotMatched.executionId} not found while creating new crawl")

        val crawl = CrawlEntity.new {
            url = crawlNotMatched.url
            type = CrawlType.INVALID
            this.execution = execution
        }

        translator.translate(crawl) as Crawl.Invalid
    }

    fun activate(crawlActivate: CrawlActivate): Crawl.Running = transaction(database) {
        val crawl = CrawlEntity.findByIdAndUpdate(crawlActivate.crawlId.toUUID()) {
            it.type = CrawlType.RUNNING
            it.start = crawlActivate.start.toJavaInstant()
        } ?: error("Crawl ${crawlActivate.crawlId} not found while activating")

        translator.translate(crawl) as Crawl.Running
    }

    fun complete(crawlComplete: CrawlComplete): Crawl.Completed = transaction(database) {
        val crawl = CrawlEntity.findByIdAndUpdate(crawlComplete.crawlId.toUUID()) {
            it.type = CrawlType.COMPLETED
            it.end = crawlComplete.end.toJavaInstant()
            it.title = crawlComplete.title
        } ?: error("Crawl ${crawlComplete.crawlId} not found while completing")

        val pending = crawlComplete.pending.map { child ->
            CrawlEntity.new {
                url = child.url
                type = CrawlType.PENDING
                this.execution = crawl.execution
            }.also { it.parent = SizedCollection(crawl) }
        }

        val invalid = crawlComplete.invalid.map { child ->
            CrawlEntity.new {
                url = child.url
                type = CrawlType.INVALID
                this.execution = crawl.execution
            }.also { it.parent = SizedCollection(crawl) }
        }

        crawl.children = SizedCollection(pending + invalid)

        translator.translate(crawl) as Crawl.Completed
    }

    fun update(crawlUpdate: CrawlUpdate): Crawl.Completed = transaction(database) {
        val crawl = CrawlEntity.findById(crawlUpdate.crawlId.toUUID())
            ?: error("Crawl ${crawlUpdate.crawlId} not found while updating")

        val childId = crawlUpdate.child.crawlId.toUUID()
        val child = CrawlEntity.findByIdAndUpdate(childId) {
            it.type = CrawlType.COMPLETED
            it.end = crawlUpdate.child.end.toJavaInstant()
            it.title = crawlUpdate.child.title
        } ?: error("Crawl ${crawlUpdate.child.crawlId} not found while updating")

        val updatedChildren = crawl.children.toList().map { if (it.id.value == childId) child else it }
        crawl.children = SizedCollection(updatedChildren)

        translator.translate(crawl) as Crawl.Completed
    }

    fun update(crawlUpdate: CrawlUpdateRunning): Crawl.Completed = transaction(database) {
        val crawl = CrawlEntity.findById(crawlUpdate.crawlId.toUUID())
            ?: error("Crawl ${crawlUpdate.crawlId} not found while updating")

        val childId = crawlUpdate.child.crawlId.toUUID()
        val child = CrawlEntity.findByIdAndUpdate(childId) {
            it.type = CrawlType.RUNNING
            it.start = crawlUpdate.child.start.toJavaInstant()
        } ?: error("Crawl ${crawlUpdate.child.crawlId} not found while updating")

        val updatedChildren = crawl.children.toList().map { if (it.id.value == childId) child else it }
        crawl.children = SizedCollection(updatedChildren)

        translator.translate(crawl) as Crawl.Completed
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}

data class ExecutionCreate(
    val recordId: String,
    val baseUrl: String,
    val regexp: String,
    val start: Instant,
)

data class ExecutionInvalidate(val executionId: String, val end: Instant)

data class ExecutionStart(val executionId: String)

data class ExecutionComplete(val executionId: String, val crawlId: String, val end: Instant)

data class CrawlCreate(val executionId: String, val url: String, val parentId: String?)

data class CrawlActivate(val crawlId: String, val start: Instant)

data class CrawlNotMatched(val executionId: String, val url: String)

data class CrawlUpdate(val crawlId: String, val child: Crawl.Completed)

data class CrawlUpdateRunning(val crawlId: String, val child: Crawl.Running)

data class CrawlComplete(
    val crawlId: String,
    val title: String?,
    val end: Instant,
    val pending: List<Crawl.Pending>,
    val invalid: List<Crawl.Invalid>,
)