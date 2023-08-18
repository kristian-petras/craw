package application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import model.Event
import model.Execution
import model.ExecutorSearch
import okhttp3.OkHttpClient
import okhttp3.Request
import utility.TimeProvider
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Executor(timeProvider: TimeProvider) {
    private val scheduler = Scheduler(timeProvider)

    private val client = OkHttpClient()
    private val builder = Request.Builder()

    private val crawler = Crawler {
        val request = builder.url(it).get().build()
        client.newCall(request).execute().body.string()
    }

    fun remove(recordId: Int) = scheduler.remove(recordId)
    fun schedule(search: ExecutorSearch, timestamp: Instant) = scheduler.schedule(Event(timestamp, search))

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe(): Flow<Pair<Int, Execution>> = scheduler
        .subscribe()
        .flatMapMerge {
            val records = crawler.recursiveCrawl(it.url, it.boundaryRegExp)
            val totalTime = records
                .sumOf { record -> Duration.parse(record.crawlTime).toDouble(DurationUnit.MILLISECONDS) }
                .toDuration(DurationUnit.MILLISECONDS)

            val execution = Execution(records, totalTime.toIsoString())
            flowOf(it.id to execution)
        }
}

