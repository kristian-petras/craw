package com.craw.schema.rest

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WebsiteRecords(val records: List<WebsiteRecord>)

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
    val url: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
)

@Serializable
data class WebsiteRecordDelete(val recordId: String)

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