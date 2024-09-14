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

    fun createRecord(record: RecordCreate): RecordState = transaction(database) {
        val new = RecordEntity.new {
            url = record.baseUrl
            regexp = record.regexp
            periodicity = record.periodicity
            label = record.label
            tags = record.tags
            active = record.active
        }

        translator.translate(new)
    }


    fun createExecution(
        recordId: String,
        regexp: String,
        url: String,
        start: Instant
    ): Execution.Pending = transaction(database) {
        val record = RecordEntity.findById(recordId.toUUID())
            ?: error("Record $recordId not found while creating new execution")

        val new = ExecutionEntity.new {
            type = ExecutionType.PENDING
            this.url = url
            this.regexp = regexp
            this.start = start.toJavaInstant()
            this.record = record
        }

        translator.translate(new) as Execution.Pending
    }

    fun createCrawl(
        executionId: String,
        url: String,
        parentId: String?,
    ): Crawl.Pending = createCrawl(executionId, url, parentId, CrawlType.PENDING) as Crawl.Pending

    fun createInvalidCrawl(
        executionId: String,
        url: String,
        parentId: String,
    ): Crawl.Invalid = createCrawl(executionId, url, parentId, CrawlType.INVALID) as Crawl.Invalid

    private fun createCrawl(
        executionId: String,
        url: String,
        parentId: String?,
        crawlType: CrawlType,
    ): Crawl = transaction(database) {
        val execution = ExecutionEntity.findById(executionId.toUUID())
            ?: error("Execution $executionId not found while creating new crawl")

        val new = CrawlEntity.new {
            this.type = crawlType
            this.url = url
            this.execution = execution
            if (parentId != null) {
                val parent = CrawlEntity.findById(parentId.toUUID()) ?: error("Parent $parentId not found")
                this.parent = SizedCollection(parent)
            }
        }

        translator.translate(new)
    }

    fun getRecords(): List<RecordState> = transaction(database) {
        RecordEntity.all()
    }.map { translator.translate(it) }

    fun getRecord(id: String): RecordState? = transaction(database) {
        RecordEntity.findById(id.toUUID())
    }?.let { translator.translate(it) }

    fun getExecution(executionId: String): Execution? = transaction(database) {
        ExecutionEntity.findById(executionId.toUUID())
    }?.let { translator.translate(it) }

    fun getCrawl(crawlId: String): Crawl? = transaction(database) {
        CrawlEntity.findById(crawlId.toUUID())
    }?.let { translator.translate(it) }


    fun updateRecord(record: RecordUpdate): Boolean = transaction(database) {
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

    fun startExecution(executionId: String): Execution.Running = transaction(database) {
        ExecutionEntity.findByIdAndUpdate(executionId.toUUID()) {
            it.type = ExecutionType.RUNNING
        }
    }?.let { translator.translate(it) as Execution.Running } ?: error("Execution $executionId not found while starting")

    fun completeExecution(executionId: String, end: Instant): Execution.Completed = transaction(database) {
        ExecutionEntity.findByIdAndUpdate(executionId.toUUID()) {
            it.type = ExecutionType.COMPLETED
            it.end = end.toJavaInstant()
        }
    }?.let { translator.translate(it) as Execution.Completed }
        ?: error("Execution $executionId not found while completing")

    fun invalidateCrawl(crawlId: String, error: String): Crawl.Invalid = transaction(database) {
        CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
            it.type = CrawlType.INVALID
            it.error = error
        }
    }?.let { translator.translate(it) as Crawl.Invalid } ?: error("Crawl $crawlId not found while invalidating")

    fun startCrawl(crawlId: String, start: Instant): Crawl.Running = transaction(database) {
        CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
            it.type = CrawlType.RUNNING
            it.start = start.toJavaInstant()
        }
    }?.let { translator.translate(it) as Crawl.Running } ?: error("Crawl $crawlId not found while starting")

    fun completeCrawl(crawlId: String, title: String?, end: Instant): Crawl.Completed = transaction(database) {
        CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
            it.type = CrawlType.COMPLETED
            it.end = end.toJavaInstant()
            it.title = title
        }
    }?.let { translator.translate(it) as Crawl.Completed } ?: error("Crawl $crawlId not found while completing")

    fun deleteRecord(id: String): Boolean = transaction(database) {
        val record = RecordEntity.findById(id.toUUID()) ?: return@transaction false
        record.executions.forEach { deleteExecution(it.id.value.toString()) }
        record.delete()
        true
    }

    fun deleteExecution(executionId: String): Boolean = transaction(database) {
        val execution = ExecutionEntity.findById(executionId.toUUID()) ?: return@transaction false
        execution.crawls.forEach { deleteCrawl(it.id.value.toString()) }
        execution.delete()
        true
    }

    fun deleteCrawl(crawlId: String): Boolean = transaction(database) {
        val crawl = CrawlEntity.findById(crawlId.toUUID()) ?: return@transaction false
        crawl.delete()
        true
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}