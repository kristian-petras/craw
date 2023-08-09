package application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import model.Event
import model.Execution
import model.WebsiteRecord
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Executor(private val timeProvider: TimeProvider) {
    private val scheduler = Scheduler<WebsiteRecord>(timeProvider)
    private val crawler = Crawler()

    private val counter = AtomicLong()
    fun schedule(record: WebsiteRecord, timestamp: Instant) = scheduler.schedule(Event(timestamp, record))

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe(): Flow<Execution> = scheduler
        .subscribe()
        .flatMapMerge {
            val records = crawler.recursiveCrawl(it.url, it.boundaryRegExp)
            val totalTime = records
                .sumOf { record -> record.crawlTime.toDouble(DurationUnit.MILLISECONDS) }
                .toDuration(DurationUnit.MILLISECONDS)

            val execution = Execution(it.id, counter.incrementAndGet(), records, totalTime, timeProvider.now(), false)
            flowOf(execution)
        }
}

