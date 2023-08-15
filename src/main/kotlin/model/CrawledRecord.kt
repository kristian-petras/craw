@file:UseSerializers(DurationSerializer::class)
package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import utility.DurationSerializer

@Serializable
data class CrawledRecord(
    val url: String,
    val crawlTime: String,
    val title: String,
    val links: List<String>,
    val matchedLinks: List<String>
)