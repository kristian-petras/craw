package application

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import model.CrawledRecord
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.time.measureTime

class Crawler(private val client: HttpClient = HttpClient()) {
    /**
     * Parallel and recursive crawling
     */
    suspend fun recursiveCrawl(
        requestUrl: String,
        matcher: String,
    ): List<CrawledRecord> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                val record = crawl(requestUrl, matcher) ?: return@withContext emptyList()
                val next =
                    record.matchedLinks.filter { it != requestUrl }.map {
                        async {
                            recursiveCrawl(it, matcher)
                        }
                    }

                listOf(record) + next.awaitAll().flatten()
            }
        }

    suspend fun crawl(
        requestUrl: String,
        matcher: String,
    ): CrawledRecord? {
        logger.info("Started to crawl request $requestUrl")
        val regex = Regex(matcher)
        val links: List<String>
        val matchedLinks: List<String>
        val title: String
        val crawlTime =
            measureTime {
                val response =
                    try {
                        client.get(requestUrl)
                    } catch (e: Exception) {
                        println("An error occurred: ${e.message}")
                        return null
                    }
                val payload =
                    if (response.status.isSuccess()) {
                        response.bodyAsText()
                    } else {
                        logger.warn("Response for request $requestUrl failed with ${response.status}")
                        return null
                    }
                val document = Jsoup.parse(payload)
                title = document.title()
                links =
                    document.select("a[href]")
                        .map { link -> link.attr("href") }
                        .map {
                            if (it.startsWith("http://") || it.startsWith("https://")) {
                                it.toString()
                            } else {
                                val base = URI(requestUrl)
                                "${base.scheme}://${base.authority}$it"
                            }
                        }
                        .filter { it.isNotBlank() }
                matchedLinks =
                    links
                        .filter { regex.matches(it) }
            }

        return CrawledRecord(requestUrl, crawlTime.toIsoString(), title, links, matchedLinks)
            .also { logger.info("Crawler finished crawling of $requestUrl with $it") }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}
