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
            url = record.url,
            regexp = record.regexp,
            periodicity = record.periodicity.toIsoString(),
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

    fun translate(
        oldRecord: RecordState,
        recordUpdate: WebsiteRecordUpdate,
    ): RecordUpdate =
        RecordUpdate(
            recordId = recordUpdate.recordId,
            baseUrl = recordUpdate.url ?: oldRecord.url,
            regexp = recordUpdate.regexp ?: oldRecord.regexp,
            periodicity = recordUpdate.periodicity ?: oldRecord.periodicity.toIsoString(),
            label = recordUpdate.label ?: oldRecord.label,
            active = recordUpdate.active ?: oldRecord.active,
            tags = recordUpdate.tags ?: oldRecord.tags,
        )

    private fun translate(execution: Execution): WebsiteExecution =
        when (execution) {
            is Execution.Completed ->
                WebsiteExecution(
                    url = execution.url.toString(),
                    start = execution.start,
                    end = execution.end,
                    title = execution.crawl.toTitle(),
                    links = execution.crawl.toLinks(),
                )

            is Execution.Running ->
                WebsiteExecution(
                    url = execution.url.toString(),
                    start = execution.start,
                    end = null,
                    title = execution.crawl.toTitle(),
                    links = execution.crawl.toLinks(),
                )

            is Execution.Pending ->
                WebsiteExecution(
                    url = execution.url.toString(),
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
            is Crawl.Completed -> listOf(url.toString()) + crawls.flatMap { it.toLinks() }
            is Crawl.Running, is Crawl.Pending, is Crawl.Invalid -> emptyList()
        }
}
