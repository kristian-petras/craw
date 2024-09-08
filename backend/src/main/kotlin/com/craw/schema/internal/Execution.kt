package com.craw.schema.internal

import kotlinx.datetime.Instant

sealed interface Execution {
    val executionId: String

    data class Scheduled(
        override val executionId: String,
        val baseUrl: String,
        val regexp: String,
        val start: Instant,
    ) : Execution

    data class Running(
        override val executionId: String,
        val baseUrl: String,
        val regexp: String,
        val start: Instant,
        val crawl: Crawl,
    ) : Execution

    data class Completed(
        override val executionId: String,
        val baseUrl: String,
        val regexp: String,
        val start: Instant,
        val end: Instant,
        val crawl: Crawl.Completed,
    ) : Execution

    data class Removed(
        override val executionId: String,
        val baseUrl: String,
        val start: Instant,
        val end: Instant,
    ) : Execution
}
