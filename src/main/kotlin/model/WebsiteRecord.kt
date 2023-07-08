package model

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.asResult
import kotlin.time.Duration

@Serializable
data class WebsiteRecord(
    val url: String,
    val boundaryRegExp: String,
    val periodicity: Duration,
    val label: String,
    val active: Boolean,
    val tags: List<String>
) {
    companion object {
        val lens = Body.auto<WebsiteRecord>().toLens().asResult()
    }
}