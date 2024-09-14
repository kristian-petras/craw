package com.craw.schema.internal

import kotlinx.datetime.Instant

sealed interface Execution {
    val executionId: String
    val regexp: String
    val crawl: Crawl

    data class Pending(
        override val executionId: String,
        override val regexp: String,
        override val crawl: Crawl.Pending,
        val start: Instant,
    ) : Execution {
        val url = crawl.url
    }

    data class Running(
        override val executionId: String,
        override val regexp: String,
        override val crawl: Crawl,
        val start: Instant
    ) : Execution {
        val url = crawl.url
    }

    data class Completed(
        override val executionId: String,
        override val regexp: String,
        override val crawl: Crawl.Completed,
    ) : Execution {
        val start = crawl.start
        val end = crawl.end
        val url = crawl.url
    }
}
