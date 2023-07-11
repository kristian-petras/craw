package application

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import model.CrawledRecord
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import java.time.Instant

class Executor(client: HttpHandler) {

    private val timeProvider = TimeProvider { Instant.now() }
    private val scheduler = Scheduler<Unit>(timeProvider)
    private val crawler = Crawler(client)

    suspend fun crawl(request: Request, matcher: String) : List<CrawledRecord> = coroutineScope {
        val record = crawler.crawl(request, matcher)
        val next = record.links.map {
            async(Dispatchers.IO) {
                crawl(Request(GET, it), matcher)
            }
        }

        listOf(record) + next.awaitAll().flatten()
    }

}