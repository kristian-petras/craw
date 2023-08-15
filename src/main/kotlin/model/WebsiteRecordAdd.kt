@file:UseSerializers(DurationSerializer::class)
package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class WebsiteRecordAdd(
    val url: String,
    val boundaryRegExp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>
)