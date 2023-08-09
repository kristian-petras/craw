package model

import java.time.Instant
import kotlin.time.Duration

/**
 * Execution is used to store crawled
 */
data class Execution(
    val recordId: Long,
    val executionId: Long,
    val crawledRecords: List<CrawledRecord>,
    val totalTime: Duration,
    val lastExecutionTimestamp: Instant,
    val lastExecutionStatus: Boolean
)