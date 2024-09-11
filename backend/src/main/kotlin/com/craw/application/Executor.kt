package com.craw.application

import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.ExecutionUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.datetime.Instant
import com.craw.utility.TimeProvider
import com.craw.utility.between
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

class Executor(
    private val timeProvider: TimeProvider,
    private val crawler: Crawler,
    private val repository: Repository,
) {
    private val queue = mutableListOf<TimedExecutionSchedule>()
    private val reschedule = Channel<Unit>(Channel.UNLIMITED)
    private val executions = Channel<ExecutionUpdate>(Channel.UNLIMITED)

    fun schedule(schedule: ExecutionSchedule) {
        val now = timeProvider.now()
        remove(schedule.recordId)

        val executionCreate = ExecutionCreate(schedule.recordId, schedule.baseUrl, schedule.regexp.pattern, now)
        val execution = repository.create(executionCreate)

        queue.add(TimedExecutionSchedule(now, schedule))
        queue.sortBy { it.executeAt }

        execution.publishState(schedule.recordId)
    }

    fun remove(recordId: String) {
        val removed = queue.removeIf { it.schedule.recordId == recordId }

        if (removed) {
            val state = repository.invalidate(ExecutionInvalidate(recordId, timeProvider.now()))
            state.publishState(recordId)
        }
    }

    /**
     * Returns a flow of execution updates.
     * After scheduling a new execution, the flow will emit the states as they are being crawled.
     * Should be called only once.
     */
    fun subscribe(): Flow<ExecutionUpdate> = channelFlow {
        launch { startExecutor() }
        executions.consumeAsFlow().collect { send(it) }
    }

    /**
     * Should be called only once.
     * Starts the executor loop.
     * The loop will wait for the next event to be executed and then crawl it.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun startExecutor() {
        while (coroutineContext.isActive) {
            select {
                reschedule.onReceive {} // restart the waiting in case something new was scheduled
                onTimeout(untilNextEvent(timeProvider.now())) {
                    val event = queue.removeFirstOrNull()
                    if (event != null) {
                        val (_, schedule) = event

                        val execution = repository.start(ExecutionStart(schedule.recordId))

                        crawler.crawl(
                            executionId = execution.executionId,
                            url = schedule.baseUrl,
                            regex = schedule.regexp
                        )
                            .applyUpdatesOn(execution)
                            .map { ExecutionUpdate(schedule.recordId, it) }
                            .collect { executions.send(it) }
                    }
                }
            }
        }
    }

    private fun Flow<Crawl>.applyUpdatesOn(start: Execution.Running): Flow<Execution> =
        runningFold(start) { acc, crawl -> acc.copy(crawl = crawl) }
            .map {
                if (it.crawl is Crawl.Completed) {
                    repository.complete(ExecutionComplete(it.executionId, it.crawl.crawlId, timeProvider.now()))
                } else {
                    it
                }
            }

    private fun Execution.publishState(recordId: String) {
        val update = ExecutionUpdate(recordId, this)
        executions.trySend(update)
        reschedule.trySend(Unit)
    }

    private fun untilNextEvent(now: Instant): Duration {
        val nextEvent = queue.firstOrNull()?.executeAt ?: return Duration.INFINITE
        return Duration.between(now, nextEvent)
    }

    private data class TimedExecutionSchedule(
        val executeAt: Instant,
        val schedule: ExecutionSchedule,
    )
}
