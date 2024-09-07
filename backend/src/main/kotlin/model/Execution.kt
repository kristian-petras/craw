@file:UseSerializers(DurationSerializer::class)

package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import utility.DurationSerializer

/**
 * Execution is used to store crawled
 */
@Serializable
data class Execution(
    val crawledRecords: List<CrawledRecord>,
    val totalTime: String,
)
