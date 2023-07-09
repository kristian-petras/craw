package model

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.asResult
import kotlin.time.Duration

/**
 * Website record used to keep track of websites to crawl.
 * URL is used as a unique identifier of a record.
 *
 * - URL - where the crawler should start.
 * - Boundary RegExp - when the crawler found a link, the link must match this expression in order to be followed.
 * User is required to provide value for this.
 * - Periodicity (minute, hour, day) - how often should the site be crawled.
 * - Label - user given label.
 * - Active / Inactive - if inactive, the site is not crawled based on the Periodicity.
 * - Tags - user given strings.
 * - Last execution - Timestamp of last crawler execution.
 * - Execution status - Status of last crawling.
 */
@Serializable
data class WebsiteRecord(
    val url: String,
    val boundaryRegExp: String,
    val periodicity: Duration,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
    val lastExecution: String,
    val executionStatus: Boolean
) {
    companion object {
        val lens = Body.auto<WebsiteRecord>().toLens().asResult()
    }
}