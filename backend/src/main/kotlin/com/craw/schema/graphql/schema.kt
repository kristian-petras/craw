package com.craw.schema.graphql

import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.operations.Query

interface Query : Query {
    fun websites(): List<WebPage>

    fun nodes(webPages: List<ID>): List<Node>
}

data class WebPage(
    val identifier: ID,
    val label: String,
    val url: String,
    val regexp: String,
    val tags: List<String>,
    val active: Boolean,
)

data class Node(
    val title: String?,
    val url: String,
    val crawlTime: String?,
    val links: List<Node>,
    val owner: WebPage,
)
