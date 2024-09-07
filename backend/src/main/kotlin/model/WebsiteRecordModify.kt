@file:UseSerializers(DurationSerializer::class)

package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import utility.DurationSerializer

@Serializable
data class WebsiteRecordModify(
    val id: Int,
    val url: String?,
    val boundaryRegExp: String?,
    val periodicity: String?,
    val label: String?,
    val active: Boolean?,
    val tags: List<String>?,
) {
    fun from(record: WebsiteRecord) =
        record.copy(
            url = url ?: record.url,
            boundaryRegExp = boundaryRegExp ?: record.boundaryRegExp,
            periodicity = periodicity ?: record.periodicity,
            label = label ?: record.label,
            active = active ?: record.active,
            tags = tags ?: record.tags,
        )
}
