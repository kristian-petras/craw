package model

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class WebsiteRecord(
    val url: String,
    val boundaryRegExp: String,
    val periodicity: Duration,
    val label: String,
    val active: Boolean,
    val tags: List<String>
)