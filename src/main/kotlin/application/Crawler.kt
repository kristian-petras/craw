package application

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import model.CrawledRecord
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

class Crawler(private val client: HttpClient = HttpClient()) {
    /**
     * Parallel and recursive crawling
     */
    suspend fun recursiveCrawl(requestUrl: String, matcher: String) : List<CrawledRecord> = coroutineScope {
        val record = crawl(requestUrl, matcher)
        val next = record.matchedLinks.map {
            async(Dispatchers.IO) {
                recursiveCrawl(it, matcher)
            }
        }

        listOf(record) + next.awaitAll().flatten()
    }

    suspend fun crawl(requestUrl: String, matcher: String) : CrawledRecord {
        logger.info("Started to crawl request $requestUrl")
        val regex = Regex(matcher)
        val links: List<String>
        val matchedLinks: List<String>
        val title: String
        val crawlTime = measureTime {
            val response = client.get(requestUrl).bodyAsText()
            val document = Jsoup.parse(response)
            title = document.title()
            links = document.select("a[href]")
                .map { link -> link.attr("href") }
            matchedLinks = links
                .filter { regex.matches(it) }
        }

        return CrawledRecord(requestUrl, crawlTime.toIsoString(), title, links, matchedLinks)
            .also { logger.info("Crawler finished crawling of $requestUrl with $it") }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}

