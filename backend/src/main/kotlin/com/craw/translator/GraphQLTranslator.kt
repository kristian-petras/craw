package com.craw.translator

import com.craw.schema.graphql.Node
import com.craw.schema.graphql.WebPage
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState
import com.expediagroup.graphql.generator.scalars.ID

class GraphQLTranslator {
    fun translate(record: RecordState): WebPage = WebPage(
        identifier = ID(record.recordId),
        label = record.label,
        url = record.baseUrl,
        regexp = record.regexp,
        tags = record.tags,
        active = record.active,
    )

    fun translate(owner: WebPage, execution: Execution): Node = when (execution) {
        is Execution.Completed -> translate(owner, execution.crawl)
        is Execution.Running -> translate(owner, execution.crawl)
        is Execution.Scheduled -> Node(
            title = null,
            url = owner.url,
            crawlTime = null,
            links = emptyList(),
            owner = owner
        )
    }

    private fun translate(owner: WebPage, crawl: Crawl): Node = when (crawl) {
        is Crawl.Completed -> Node(
            title = crawl.title,
            url = crawl.url,
            crawlTime = crawl.crawlTime.toString(),
            links = crawl.crawls.map { translate(owner, it) },
            owner = owner
        )

        is Crawl.Running, is Crawl.Pending, is Crawl.Invalid -> Node(
            title = null,
            url = crawl.url,
            crawlTime = null,
            links = emptyList(),
            owner = owner
        )
    }

    private fun Crawl.toTitle(): String? = when (this) {
        is Crawl.Completed -> title
        is Crawl.Running, is Crawl.Invalid, is Crawl.Pending -> null
    }
}