package com.craw.translator

import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordState
import com.craw.schema.internal.RecordUpdate
import com.craw.schema.rest.WebsiteExecution
import com.craw.schema.rest.WebsiteRecord
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordUpdate

class RestTranslator {
    fun translate(record: RecordState): WebsiteRecord =
        WebsiteRecord(
            recordId = record.recordId,
            url = record.baseUrl,
            regexp = record.regexp,
            periodicity = record.periodicity,
            label = record.label,
            active = record.active,
            tags = record.tags,
            executions = record.executions.map { execution -> translate(execution) },
        )

    fun translate(recordCreate: WebsiteRecordCreate): RecordCreate =
        RecordCreate(
            baseUrl = recordCreate.url,
            regexp = recordCreate.regexp,
            periodicity = recordCreate.periodicity,
            label = recordCreate.label,
            active = recordCreate.active,
            tags = recordCreate.tags,
        )

    fun translate(recordUpdate: WebsiteRecordUpdate): RecordUpdate =
        RecordUpdate(
            recordId = recordUpdate.recordId,
            baseUrl = recordUpdate.url,
            regexp = recordUpdate.regexp,
            periodicity = recordUpdate.periodicity,
            label = recordUpdate.label,
            active = recordUpdate.active,
            tags = recordUpdate.tags,
        )

    private fun translate(execution: Execution): WebsiteExecution =
        when (execution) {
            is Execution.Completed ->
                WebsiteExecution(
                    url = execution.url,
                    start = execution.start,
                    end = execution.end,
                    title = execution.crawl.toTitle(),
                    links = execution.crawl.toLinks(),
                )

            is Execution.Running ->
                WebsiteExecution(
                    url = execution.url,
                    start = execution.start,
                    end = null,
                    title = execution.crawl.toTitle(),
                    links = execution.crawl.toLinks(),
                )

            is Execution.Pending ->
                WebsiteExecution(
                    url = execution.url,
                    start = execution.start,
                    end = null,
                    title = null,
                    links = emptyList(),
                )
        }

    private fun Crawl.toTitle(): String? =
        when (this) {
            is Crawl.Completed -> title
            is Crawl.Running, is Crawl.Invalid, is Crawl.Pending -> null
        }

    private fun Crawl.toLinks(): List<String> =
        when (this) {
            is Crawl.Completed -> listOf(url) + crawls.flatMap { it.toLinks() }
            is Crawl.Running, is Crawl.Pending, is Crawl.Invalid -> emptyList()
        }
}
