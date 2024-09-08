package com.craw

import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.Record
import com.craw.schema.rest.WebsiteExecution
import com.craw.schema.rest.WebsiteRecord

class RestTranslator {
    fun translate(records: List<Record>): List<WebsiteRecord> = records.map { record -> translate(record) }

    fun translate(record: Record): WebsiteRecord = WebsiteRecord(
        recordId = record.recordId,
        url = record.baseUrl,
        regexp = record.regexp,
        periodicity = record.periodicity,
        label = record.label,
        active = record.active,
        tags = record.tags,
        executions = record.executions.map { execution -> translate(execution) }
    )

    private fun translate(execution: Execution): WebsiteExecution = when (execution) {
        is Execution.Completed -> WebsiteExecution(
            url = execution.baseUrl,
            start = execution.start,
            end = execution.end,
            title = execution.crawl.toTitle(),
            links = execution.crawl.toLinks()
        )

        is Execution.Running -> WebsiteExecution(
            url = execution.baseUrl,
            start = execution.start,
            end = null,
            title = execution.crawl.toTitle(),
            links = execution.crawl.toLinks()
        )

        is Execution.Scheduled -> WebsiteExecution(
            url = execution.baseUrl,
            start = execution.start,
            end = null,
            title = null,
            links = emptyList()
        )
    }

    private fun Crawl.toTitle(): String? = when (this) {
        is Crawl.Completed -> title
        is Crawl.Running, is Crawl.Invalid, is Crawl.Pending -> null
    }

    private fun Crawl.toLinks(): List<String> = when (this) {
        is Crawl.Completed -> listOf(url) + crawls.flatMap { it.toLinks() }
        is Crawl.Running, is Crawl.Pending, is Crawl.Invalid -> emptyList()
    }
}