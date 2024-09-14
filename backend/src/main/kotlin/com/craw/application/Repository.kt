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
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class Repository(private val translator: DatabaseTranslator, private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(RecordsTable, ExecutionsTable, CrawlsTable, CrawlRelationsTable)
        }
    }

    fun createRecord(record: RecordCreate): RecordState =
        transaction(database) {
            exposedLogger.info("Creating new record for url ${record.baseUrl}")
            val new = RecordEntity.new {
                url = record.baseUrl
                regexp = record.regexp
                periodicity = record.periodicity
                label = record.label
                tags = record.tags
                active = record.active
            }

            exposedLogger.info("Created new record $new")
            new
        }.let { translator.translate(it) }

    fun createExecution(
        recordId: String,
        regexp: String,
        url: String,
        start: Instant,
    ): Execution.Pending =
        transaction(database) {
            exposedLogger.info("Creating new execution for record $recordId")
            val record = findRecord(recordId) ?: error("Record $recordId not found while creating new execution")

            val new = ExecutionEntity.new {
                type = ExecutionType.PENDING
                this.url = url
                this.regexp = regexp
                this.start = start.toJavaInstant()
                this.record = record
            }

            exposedLogger.info("Created new execution $new")
            new
        }.let { translator.translate(it) as Execution.Pending }

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
    ): Crawl =
        transaction(database) {
            exposedLogger.info("Creating $crawlType crawl for execution $executionId with parent $parentId")
            val execution =
                findExecution(executionId) ?: error("Execution $executionId not found while creating new crawl")

            val new =
                CrawlEntity.new {
                    this.type = crawlType
                    this.url = url
                    this.execution = execution
                    if (parentId != null) {
                        val parent = findCrawl(parentId) ?: error("Parent $parentId not found")
                        this.parent = SizedCollection(parent)
                    }
                }

            exposedLogger.info("Created new crawl $new")
            translator.translate(new)
        }

    fun getRecords(): List<RecordState> =
        transaction(database) {
            exposedLogger.info("Getting all records")
            val records = RecordEntity.all().toList()
            exposedLogger.info("Got ${records.size} records $records")
            records
        }.map { translator.translate(it) }

    fun getRecord(recordId: String): RecordState? =
        transaction(database) { findRecord(recordId) }?.let { translator.translate(it) }

    fun getExecution(executionId: String): Execution? =
        transaction(database) { findExecution(executionId) }?.let { translator.translate(it) }

    fun getCrawl(crawlId: String): Crawl? =
        transaction(database) { findCrawl(crawlId) }?.let { translator.translate(it) }

    fun updateRecord(record: RecordUpdate): RecordState? =
        transaction(database) {
            exposedLogger.info("Updating record ${record.recordId}")
            RecordEntity.findByIdAndUpdate(record.recordId.toUUID()) {
                it.url = record.baseUrl
                it.regexp = record.regexp
                it.periodicity = record.periodicity
                it.label = record.label
                it.active = record.active
                it.tags = record.tags
            }.also { exposedLogger.info("Updated record $it") }
        }?.let { translator.translate(it) }

    fun startExecution(executionId: String): Execution.Running =
        transaction(database) {
            exposedLogger.info("Starting execution $executionId")
            ExecutionEntity.findByIdAndUpdate(executionId.toUUID()) {
                it.type = ExecutionType.RUNNING
            }.also { exposedLogger.info("Started execution $it") }
        }?.let { translator.translate(it) as Execution.Running }
            ?: error("Execution $executionId not found while starting")

    fun completeExecution(
        executionId: String,
        end: Instant,
    ): Execution.Completed =
        transaction(database) {
            exposedLogger.info("Completing execution $executionId")
            ExecutionEntity.findByIdAndUpdate(executionId.toUUID()) {
                it.type = ExecutionType.COMPLETED
                it.end = end.toJavaInstant()
            }.also { exposedLogger.info("Completed execution $it") }
        }?.let { translator.translate(it) as Execution.Completed }
            ?: error("Execution $executionId not found while completing")

    fun invalidateCrawl(
        crawlId: String,
        error: String,
    ): Crawl.Invalid =
        transaction(database) {
            exposedLogger.info("Invalidating crawl $crawlId")
            CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
                it.type = CrawlType.INVALID
                it.error = error
            }.also { exposedLogger.info("Invalidated crawl $it") }
        }?.let { translator.translate(it) as Crawl.Invalid } ?: error("Crawl $crawlId not found while invalidating")

    fun startCrawl(
        crawlId: String,
        start: Instant,
    ): Crawl.Running =
        transaction(database) {
            exposedLogger.info("Starting crawl $crawlId")
            CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
                it.type = CrawlType.RUNNING
                it.start = start.toJavaInstant()
            }.also { exposedLogger.info("Started crawl $it") }
        }?.let { translator.translate(it) as Crawl.Running } ?: error("Crawl $crawlId not found while starting")

    fun completeCrawl(
        crawlId: String,
        title: String?,
        end: Instant,
    ): Crawl.Completed =
        transaction(database) {
            exposedLogger.info("Completing crawl $crawlId")
            CrawlEntity.findByIdAndUpdate(crawlId.toUUID()) {
                it.type = CrawlType.COMPLETED
                it.end = end.toJavaInstant()
                it.title = title
            }.also { exposedLogger.info("Completed crawl $it") }
        }?.let { translator.translate(it) as Crawl.Completed } ?: error("Crawl $crawlId not found while completing")

    fun deleteRecord(id: String): Boolean =
        transaction(database) {
            exposedLogger.info("Deleting record $id")
            val record = findRecord(id) ?: return@transaction false

            record.executions.forEach { deleteExecution(it.id.value.toString()) }
            exposedLogger.info("Deleted record $id")
            record.delete()
            true
        }

    fun deleteExecution(executionId: String): Boolean =
        transaction(database) {
            exposedLogger.info("Deleting execution $executionId")
            val execution = findExecution(executionId) ?: return@transaction false

            execution.crawls.forEach { deleteCrawl(it.id.value.toString()) }
            execution.delete()
            exposedLogger.info("Deleted execution $executionId")
            true
        }

    fun deleteCrawl(crawlId: String): Boolean =
        transaction(database) {
            exposedLogger.info("Deleting crawl $crawlId")
            val crawl = findCrawl(crawlId) ?: return@transaction false

            crawl.delete()
            exposedLogger.info("Deleted crawl $crawlId")
            true
        }

    private fun findRecord(id: String): RecordEntity? {
        val record = RecordEntity.findById(id.toUUID())
        if (record == null) {
            exposedLogger.warn("Record $id not found")
        } else {
            exposedLogger.info("Found record $record")
        }
        return record
    }

    private fun findExecution(id: String): ExecutionEntity? {
        val execution = ExecutionEntity.findById(id.toUUID())
        if (execution == null) {
            exposedLogger.warn("Execution $id not found")
        } else {
            exposedLogger.info("Found execution $execution")
        }
        return execution
    }

    private fun findCrawl(id: String): CrawlEntity? {
        val crawl = CrawlEntity.findById(id.toUUID())
        if (crawl == null) {
            exposedLogger.warn("Crawl $id not found")
        } else {
            exposedLogger.info("Found crawl $crawl")
        }
        return crawl
    }

    private fun String.toUUID(): UUID = UUID.fromString(this)
}
