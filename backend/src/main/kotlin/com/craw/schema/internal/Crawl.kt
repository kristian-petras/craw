package com.craw.schema.internal

import com.craw.utility.between
import io.ktor.http.Url
import kotlinx.datetime.Instant
import kotlin.time.Duration

sealed interface Crawl {
    val crawlId: String
    val url: Url

    data class Pending(
        override val crawlId: String,
        override val url: Url,
    ) : Crawl

    data class Running(
        override val crawlId: String,
        override val url: Url,
        val start: Instant,
    ) : Crawl

    data class Completed(
        override val crawlId: String,
        override val url: Url,
        val title: String?,
        val start: Instant,
        val end: Instant,
        val crawls: List<Crawl>,
    ) : Crawl {
        val crawlTime: Duration = Duration.between(start, end)
    }

    data class Invalid(
        override val crawlId: String,
        override val url: Url,
        val error: String,
        val start: Instant,
        val end: Instant,
    ) : Crawl {
        val crawlTime: Duration = Duration.between(start, end)
    }
}
