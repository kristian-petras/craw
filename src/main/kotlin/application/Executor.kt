package application

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import model.CrawledRecord
import model.WebsiteRecord
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Executor(client: HttpHandler) {
    private val timeProvider = TimeProvider { Instant.now() }
    private val scheduler = Scheduler<WebsiteRecord>(timeProvider)
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

    suspend fun schedule(record: WebsiteRecord, timestamp: Instant) = scheduler.schedule(Event(timestamp, record))

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe() : Flow<Execution> = scheduler
        .subscribe()
        .flatMapMerge {
            val request = Request(GET, it.url)
            val records = crawl(request, it.boundaryRegExp)
            val totalTime = records
                .sumOf { record -> record.crawlTime.toDouble(DurationUnit.MILLISECONDS) }
                .toDuration(DurationUnit.MILLISECONDS)

            val execution = Execution(it, records, totalTime)
            flowOf(execution)
        }
}

data class Execution(val record: WebsiteRecord, val crawledRecords: List<CrawledRecord>, val totalTime: Duration)