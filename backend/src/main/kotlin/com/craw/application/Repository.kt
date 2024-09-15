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
            SchemaUtils.drop(CrawlRelationsTable, CrawlsTable, RecordsTable, ExecutionsTable)
            SchemaUtils.createMissingTablesAndColumns(RecordsTable, ExecutionsTable, CrawlsTable, CrawlRelationsTable)
        }
    }

    fun createRecord(record: RecordCreate): RecordState? =
        transaction(database) {
            exposedLogger.info("Creating new record for url ${record.baseUrl}")
            val new =
                RecordEntity.new {
                    url = record.baseUrl
                    regexp = record.regexp
                    periodicity = record.periodicity
                    label = record.label
                    tags = record.tags
                    active = record.active
                }

            val translated = translator.translate(new)

            if (translated != null) {
                exposedLogger.info("Created new record $translated")
            } else {
                exposedLogger.warn("Failed to translate new record")
            }
            translated
        }

    fun createExecution(
        recordId: String,
        regexp: String,
        url: String,
        start: Instant,
    ): Execution.Pending =
        transaction(database) {
            exposedLogger.info("Creating new execution for record $recordId")
            val record = findRecord(recordId) ?: error("Record $recordId not found while creating new execution")

            val execution =
                ExecutionEntity.new {
                    this.type = ExecutionType.PENDING
                    this.url = url
                    this.regexp = regexp
                    this.start = start.toJavaInstant()
                    this.record = record
                }

            CrawlEntity.new {
                this.type = CrawlType.PENDING
                this.url = url
                this.parent = SizedCollection(emptyList())
                this.execution = execution
            }

            exposedLogger.info("Created new execution ${execution.id}, creating pending crawl")

            val translated = translator.translate(execution) as Execution.Pending
            exposedLogger.info("Created new execution $translated")
            translated
        }

    fun createCrawl(
        executionId: String,
        url: String,
        parentId: String?,
    ): Crawl.Pending =
        transaction(database) {
            val execution = findExecution(executionId) ?: error("Execution $executionId not found")
            val crawl = createCrawl(execution, url, parentId, CrawlType.PENDING)
            translator.translate(crawl) as Crawl.Pending
        }

    fun createInvalidCrawl(
        executionId: String,
        url: String,
        parentId: String,
        time: Instant,
        error: String,
    ): Crawl.Invalid =
        transaction(database) {
            val execution = findExecution(executionId) ?: error("Execution $executionId not found")
            val crawl = createCrawl(execution, url, parentId, CrawlType.INVALID, time, error)
            translator.translate(crawl) as Crawl.Invalid
        }

    private fun createCrawl(
        execution: ExecutionEntity,
        url: String,
        parentId: String?,
        crawlType: CrawlType,
        time: Instant? = null,
        error: String? = null,
    ): CrawlEntity {
        exposedLogger.info("Creating $crawlType crawl for execution ${execution.id} with parent $parentId")

        val new =
            CrawlEntity.new {
                this.type = crawlType
                this.url = url
                this.execution = execution
                if (parentId != null) {
                    val parent = findCrawl(parentId) ?: error("Parent $parentId not found")
                    this.parent = SizedCollection(parent)
                }
                if (crawlType == CrawlType.INVALID) {
                    this.start = time?.toJavaInstant() ?: error("Invalid crawl must have start/end time")
                    this.end = time.toJavaInstant()
                    this.error = error ?: "Unknown error."
                }
            }

        exposedLogger.info("Created new crawl ${new.id}")
        return new
    }

    fun getRecords(): List<RecordState> =
        transaction(database) {
            exposedLogger.info("Getting all records")
            val records = RecordEntity.all().toList()
            records.map { translator.translate(it) ?: error("Integrity fail, failed to translate record $it") }.also {
                exposedLogger.info("Got ${it.size} records $it")
            }
        }

    fun getRecord(recordId: String): RecordState? =
        transaction(database) {
            findRecord(recordId)?.let { translator.translate(it) }
        }

    fun getExecution(executionId: String): Execution? =
        transaction(database) {
            findExecution(executionId)?.let { translator.translate(it) }
        }

    fun getCrawl(crawlId: String): Crawl? =
        transaction(database) {
            findCrawl(crawlId)?.let { translator.translate(it) }
        }

    fun updateRecord(record: RecordUpdate): RecordState? =
        transaction(database) {
            exposedLogger.info("Updating record ${record.recordId}")
            val uuid = record.recordId.toUUID() ?: return@transaction null
            RecordEntity.findByIdAndUpdate(uuid) {
                it.url = record.baseUrl
                it.regexp = record.regexp
                it.periodicity = record.periodicity
                it.label = record.label
                it.active = record.active
                it.tags = record.tags
            }?.let { translator.translate(it) }.also { exposedLogger.info("Updated record $it") }
        }

    fun startExecution(executionId: String): Execution.Running =
        transaction(database) {
            exposedLogger.info("Starting execution $executionId")
            val uuid = executionId.toUUID() ?: error("Invalid UUID $executionId")

            val execution =
                ExecutionEntity.findByIdAndUpdate(uuid) {
                    it.type = ExecutionType.RUNNING
                } ?: error("Execution $executionId not found while starting")

            val translated = translator.translate(execution) as Execution.Running
            exposedLogger.info("Started execution $translated")
            translated
        }

    fun completeExecution(
        executionId: String,
        end: Instant,
    ): Execution.Completed =
        transaction(database) {
            exposedLogger.info("Completing execution $executionId")
            val uuid = executionId.toUUID() ?: error("Invalid UUID $executionId")
            val execution =
                ExecutionEntity.findByIdAndUpdate(uuid) {
                    it.type = ExecutionType.COMPLETED
                    it.end = end.toJavaInstant()
                } ?: error("Execution $executionId not found while completing")

            val translated = translator.translate(execution) as Execution.Completed
            exposedLogger.info("Completed execution $translated")
            translated
        }

    fun invalidateCrawl(
        crawlId: String,
        error: String,
        end: Instant,
    ): Crawl.Invalid =
        transaction(database) {
            exposedLogger.info("Invalidating crawl $crawlId")
            val uuid = crawlId.toUUID() ?: error("Invalid UUID $crawlId")
            val crawl =
                CrawlEntity.findByIdAndUpdate(uuid) {
                    it.type = CrawlType.INVALID
                    it.error = error
                    it.end = end.toJavaInstant()
                } ?: error("Crawl $crawlId not found while invalidating")

            val translated = translator.translate(crawl) as Crawl.Invalid
            exposedLogger.info("Invalidated crawl $translated")
            translated
        }

    fun startCrawl(
        crawlId: String,
        start: Instant,
    ): Crawl.Running =
        transaction(database) {
            exposedLogger.info("Starting crawl $crawlId")
            val uuid = crawlId.toUUID() ?: error("Invalid UUID $crawlId")
            val crawl =
                CrawlEntity.findByIdAndUpdate(uuid) {
                    it.type = CrawlType.RUNNING
                    it.start = start.toJavaInstant()
                } ?: error("Crawl $crawlId not found while starting")

            val translated = translator.translate(crawl) as Crawl.Running
            exposedLogger.info("Started crawl $translated")
            translated
        }

    fun completeCrawl(
        crawlId: String,
        title: String?,
        end: Instant,
    ): Crawl.Completed =
        transaction(database) {
            exposedLogger.info("Completing crawl $crawlId")
            val uuid = crawlId.toUUID() ?: error("Invalid UUID $crawlId")
            val crawl =
                CrawlEntity.findByIdAndUpdate(uuid) {
                    it.type = CrawlType.COMPLETED
                    it.end = end.toJavaInstant()
                    it.title = title
                } ?: error("Crawl $crawlId not found while completing")
            val translated = translator.translate(crawl) as Crawl.Completed
            exposedLogger.info("Completed crawl $translated")
            translated
        }

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
        val uuid = id.toUUID() ?: return null
        val record = RecordEntity.findById(uuid)
        if (record == null) {
            exposedLogger.warn("Record $id not found")
        }
        return record
    }

    private fun findExecution(id: String): ExecutionEntity? {
        val uuid = id.toUUID() ?: return null
        val execution = ExecutionEntity.findById(uuid)
        if (execution == null) {
            exposedLogger.warn("Execution $id not found")
        }
        return execution
    }

    private fun findCrawl(id: String): CrawlEntity? {
        val uuid = id.toUUID() ?: return null
        val crawl = CrawlEntity.findById(uuid)
        if (crawl == null) {
            exposedLogger.warn("Crawl $id not found")
        }
        return crawl
    }

    private fun String.toUUID(): UUID? {
        val uuid = runCatching { UUID.fromString(this) }.getOrNull()
        if (uuid == null) {
            exposedLogger.warn("Invalid UUID $this")
        }
        return uuid
    }
}
