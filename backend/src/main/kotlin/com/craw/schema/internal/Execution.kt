package com.craw.schema.internal

import io.ktor.http.Url
import kotlinx.datetime.Instant

sealed interface Execution {
    val executionId: String
    val regexp: Regex
    val crawl: Crawl
    val url: Url

    data class Pending(
        override val executionId: String,
        override val regexp: Regex,
        override val crawl: Crawl.Pending,
        val start: Instant,
    ) : Execution {
        override val url = crawl.url
    }

    data class Running(
        override val executionId: String,
        override val regexp: Regex,
        override val crawl: Crawl,
        val start: Instant,
    ) : Execution {
        override val url = crawl.url
    }

    // crawl can be either completed or invalid
    data class Completed(
        override val executionId: String,
        override val regexp: Regex,
        override val crawl: Crawl,
        val start: Instant,
        val end: Instant,
    ) : Execution {
        override val url = crawl.url
    }
}
