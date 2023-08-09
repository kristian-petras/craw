package application

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import model.CrawledRecord
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.measureTime

class Crawler(private val client: HttpHandler) {

    /**
     * Parallel and recursive crawling
     */
    suspend fun recursiveCrawl(request: Request, matcher: String) : List<CrawledRecord> = coroutineScope {
        val record = crawl(request, matcher)
        val next = record.crawledLinks.map {
            async(Dispatchers.IO) {
                recursiveCrawl(Request(Method.GET, it), matcher)
            }
        }

        listOf(record) + next.awaitAll().flatten()
    }

    fun crawl(request: Request, matcher: String) : CrawledRecord {
        val url = request.uri.toString()
        logger.info("Started to crawl request $url")
        val regex = Regex(matcher)
        val links: List<String>
        val crawledLinks: List<String>
        val title: String
        val crawlTime = measureTime {
            val response = client(request)
            val document = Jsoup.parse(response.bodyString())
            title = document.title()
            links = document.select("a[href]")
                .map { link -> link.attr("href") }
            crawledLinks = links
                .filter { regex.matches(it) }
        }

        return CrawledRecord(url, crawlTime, title, links, crawledLinks)
            .also { logger.info("Crawler finished crawling of $url with $it") }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(Crawler::class.java)
    }
}

