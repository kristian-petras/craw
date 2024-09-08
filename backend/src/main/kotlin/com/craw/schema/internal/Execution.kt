package com.craw.schema.internal

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import utility.between
import kotlin.time.Duration

sealed interface Execution {
    val executionId: String
    val baseUrl: String
    val regexp: String
    val start: Instant

    data class Scheduled(
        override val executionId: String,
        override val baseUrl: String,
        override val regexp: String,
        override val start: Instant,
    ) : Execution

    data class Running(
        override val executionId: String,
        override val baseUrl: String,
        override val regexp: String,
        override val start: Instant,
        val crawl: Crawl,
    ) : Execution

    data class Completed(
        override val executionId: String,
        override val baseUrl: String,
        override val regexp: String,
        override val start: Instant,
        val end: Instant,
        val crawl: Crawl.Completed,
    ) : Execution {
        val crawlTime: Duration = Duration.between(start.toJavaInstant(), end.toJavaInstant())
    }
}
