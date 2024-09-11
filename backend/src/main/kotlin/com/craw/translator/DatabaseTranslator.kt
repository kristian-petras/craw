package com.craw.translator

import com.craw.schema.database.CrawlEntity
import com.craw.schema.database.CrawlType
import com.craw.schema.database.ExecutionEntity
import com.craw.schema.database.ExecutionType
import com.craw.schema.database.RecordEntity
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState
import kotlinx.datetime.toKotlinInstant

class DatabaseTranslator {
    fun translate(record: RecordEntity): RecordState {
        return RecordState(
            recordId = record.id.value.toString(),
            baseUrl = record.url,
            regexp = record.regexp,
            periodicity = record.periodicity,
            label = record.label,
            active = record.active,
            tags = record.tags,
            executions = record.executions.map { translate(it) }
        )
    }

    // TODO remove
    /**
     * Translate completed execution
     */
    fun translate(execution: ExecutionEntity, crawl: CrawlEntity): Execution.Completed = Execution.Completed(
        executionId = execution.id.value.toString(),
        baseUrl = execution.record.url,
        regexp = execution.record.regexp,
        start = execution.start.toKotlinInstant(),
        end = execution.end?.toKotlinInstant()
            ?: error("Execution ${execution.id} is completed but has no end time"),
        crawl = translate(crawl) as Crawl.Completed
    )

    fun translate(execution: ExecutionEntity): Execution = when (execution.type) {
        ExecutionType.COMPLETED -> Execution.Completed(
            executionId = execution.id.value.toString(),
            baseUrl = execution.record.url,
            regexp = execution.record.regexp,
            start = execution.start.toKotlinInstant(),
            end = execution.end?.toKotlinInstant()
                ?: error("Execution ${execution.id} is completed but has no end time"),
            crawl = execution.rootCrawl?.let { translate(it) as? Crawl.Completed }
                ?: error("Execution ${execution.id} is should have a single completed root crawl but does not")
        )

        ExecutionType.RUNNING -> Execution.Running(
            executionId = execution.id.value.toString(),
            start = execution.start.toKotlinInstant(),
            baseUrl = execution.record.url,
            regexp = execution.record.regexp,
            crawl = execution.rootCrawl?.let { translate(it) }
                ?: error("Execution ${execution.id} is should have a single root crawl but does not")
        )

        ExecutionType.SCHEDULED -> Execution.Scheduled(
            executionId = execution.id.value.toString(),
            start = execution.start.toKotlinInstant(),
            baseUrl = execution.record.url,
            regexp = execution.record.regexp
        )

        ExecutionType.INVALID -> Execution.Removed(
            executionId = execution.id.value.toString(),
            start = execution.start.toKotlinInstant(),
            end = execution.end?.toKotlinInstant()
                ?: error("Execution ${execution.id} is invalid but has no end time"),
            baseUrl = execution.record.url
        )
    }

    fun translate(crawl: CrawlEntity): Crawl = when (crawl.type) {
        CrawlType.PENDING -> Crawl.Pending(
            crawlId = crawl.id.value.toString(),
            url = crawl.url
        )

        CrawlType.RUNNING -> Crawl.Running(
            crawlId = crawl.id.value.toString(),
            url = crawl.url,
            start = crawl.start?.toKotlinInstant() ?: error("Crawl ${crawl.id} is running but has no start time")
        )

        CrawlType.COMPLETED -> Crawl.Completed(
            crawlId = crawl.id.value.toString(),
            url = crawl.url,
            title = crawl.title,
            start = crawl.start?.toKotlinInstant() ?: error("Crawl ${crawl.id} is completed but has no start time"),
            end = crawl.end?.toKotlinInstant() ?: error("Crawl ${crawl.id} is completed but has no end time"),
            crawls = crawl.children.map { translate(it) }
        )

        CrawlType.INVALID -> Crawl.Invalid(
            crawlId = crawl.id.value.toString(),
            url = crawl.url,
        )
    }
}