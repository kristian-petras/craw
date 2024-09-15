package com.craw.translator

import com.craw.schema.graphql.Node
import com.craw.schema.graphql.WebPage
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState
import com.expediagroup.graphql.generator.scalars.ID

class GraphQLTranslator {
    fun translate(record: RecordState): WebPage =
        WebPage(
            identifier = ID(record.recordId),
            label = record.label,
            url = record.url,
            regexp = record.regexp,
            tags = record.tags,
            active = record.active,
        )

    fun translate(
        owner: WebPage,
        execution: Execution,
    ): Node = translate(owner, execution.crawl)

    private fun translate(
        owner: WebPage,
        crawl: Crawl,
    ): Node =
        when (crawl) {
            is Crawl.Completed ->
                Node(
                    title = crawl.title,
                    url = crawl.url.toString(),
                    crawlTime = crawl.crawlTime.toString(),
                    links = crawl.crawls.map { translate(owner, it) },
                    owner = owner,
                )

            is Crawl.Invalid ->
                Node(
                    title = null,
                    url = crawl.url.toString(),
                    crawlTime = crawl.crawlTime.toString(),
                    links = emptyList(),
                    owner = owner,
                )

            is Crawl.Running, is Crawl.Pending ->
                Node(
                    title = null,
                    url = crawl.url.toString(),
                    crawlTime = null,
                    links = emptyList(),
                    owner = owner,
                )
        }
}
