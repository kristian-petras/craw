package com.craw.schema.rest

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WebsiteRecord(
    val recordId: String,
    val url: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
    /**
     * Sorted chronologically.
     */
    val executions: List<WebsiteExecution>,
)

@Serializable
data class WebsiteRecordCreate(
    val url: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
)

/**
 * When existing record is updated, execution is reset.
 */
@Serializable
data class WebsiteRecordUpdate(
    val recordId: String,
    val url: String? = null,
    val regexp: String? = null,
    val periodicity: String? = null,
    val label: String? = null,
    val active: Boolean? = null,
    val tags: List<String>? = null,
)

/**
 * Execution is a result of crawling the website.
 */
@Serializable
data class WebsiteExecution(
    val url: String,
    val start: Instant,
    /**
     * Null if execution is not completed.
     */
    val end: Instant?,
    val title: String?,
    val links: List<String>,
)
