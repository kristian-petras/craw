package model

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Website record used to keep track of websites to crawl.
 * [id] is used as a unique identifier of a record.
 *
 * - URL - where the crawler should start.
 * - Boundary RegExp - when the crawler found a link, the link must match this expression in order to be followed.
 * User is required to provide value for this.
 * - Periodicity (minute, hour, day) - how often should the site be crawled.
 * - Label - user given label.
 * - Active / Inactive - if inactive, the site is not crawled based on the Periodicity.
 * - Tags - user given strings.
 */
@Serializable
data class WebsiteRecord(
    val id: Long,
    val url: String,
    val boundaryRegExp: String,
    val periodicity: Duration,
    val label: String,
    val active: Boolean,
    val tags: List<String>
) : Comparable<WebsiteRecord> {
    override fun compareTo(other: WebsiteRecord): Int = id.compareTo(other.id)
}