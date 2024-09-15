package com.craw.translator

import com.craw.schema.database.CrawlEntity
import com.craw.schema.database.CrawlType
import com.craw.schema.database.ExecutionEntity
import com.craw.schema.database.ExecutionType
import com.craw.schema.database.RecordEntity
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState
import io.ktor.http.Url
import kotlinx.datetime.toKotlinInstant
import kotlin.time.Duration

class DatabaseTranslator {
    fun translate(record: RecordEntity): RecordState? {
        return RecordState(
            recordId = record.id.value.toString(),
            url = record.url,
            regexp = record.regexp,
            periodicity = Duration.parseIsoStringOrNull(record.periodicity) ?: return null,
            label = record.label,
            active = record.active,
            tags = record.tags,
            executions = record.executions.map { translate(it) },
        )
    }

    fun translate(execution: ExecutionEntity): Execution {
        val root =
            execution.crawls.toList().singleOrNull { it.root }
                ?: error("Execution ${execution.id} is missing a single root crawl")
        return when (execution.type) {
            ExecutionType.COMPLETED -> {
                val crawl = translate(root)

                val (start, end) =
                    when (crawl) {
                        is Crawl.Completed -> crawl.start to crawl.end
                        is Crawl.Invalid -> crawl.start to crawl.end
                        else -> error("Execution ${execution.id} is completed but has a non-terminated root crawl")
                    }

                Execution.Completed(
                    executionId = execution.id.value.toString(),
                    regexp = execution.record.regexp.toRegex(),
                    crawl = translate(root),
                    start = start,
                    end = end,
                )
            }

            ExecutionType.RUNNING ->
                Execution.Running(
                    executionId = execution.id.value.toString(),
                    start = execution.start.toKotlinInstant(),
                    regexp = execution.record.regexp.toRegex(),
                    crawl = translate(root),
                )

            ExecutionType.PENDING ->
                Execution.Pending(
                    executionId = execution.id.value.toString(),
                    start = execution.start.toKotlinInstant(),
                    regexp = execution.record.regexp.toRegex(),
                    crawl =
                        translate(root) as? Crawl.Pending
                            ?: error("Execution ${execution.id} is pending but does not have a pending root crawl"),
                )
        }
    }

    fun translate(crawl: CrawlEntity): Crawl =
        when (crawl.type) {
            CrawlType.PENDING ->
                Crawl.Pending(
                    crawlId = crawl.id.value.toString(),
                    url = Url(crawl.url),
                )

            CrawlType.RUNNING ->
                Crawl.Running(
                    crawlId = crawl.id.value.toString(),
                    url = Url(crawl.url),
                    start =
                        crawl.start?.toKotlinInstant()
                            ?: error("Crawl ${crawl.id} is running but has no start time"),
                )

            CrawlType.COMPLETED ->
                Crawl.Completed(
                    crawlId = crawl.id.value.toString(),
                    url = Url(crawl.url),
                    title = crawl.title,
                    start =
                        crawl.start?.toKotlinInstant()
                            ?: error("Crawl ${crawl.id} is completed but has no start time"),
                    end = crawl.end?.toKotlinInstant() ?: error("Crawl ${crawl.id} is completed but has no end time"),
                    crawls = crawl.children.map { translate(it) },
                )

            CrawlType.INVALID ->
                Crawl.Invalid(
                    crawlId = crawl.id.value.toString(),
                    url = Url(crawl.url),
                    error = crawl.error ?: error("Crawl ${crawl.id} is invalid but has no error message"),
                    start =
                        crawl.start?.toKotlinInstant()
                            ?: error("Crawl ${crawl.id} is invalid but has no start time"),
                    end = crawl.end?.toKotlinInstant() ?: error("Crawl ${crawl.id} is invalid but has no end time"),
                )
        }
}
