package com.craw.application

import com.craw.schema.internal.Execution
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import utility.TimeProvider
import utility.between
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

class Executor(private val timeProvider: TimeProvider, private val crawler: Crawler) {
    private val queue = mutableListOf<TimedExecutionSchedule>()
    private val reschedule = Channel<Unit>(Channel.UNLIMITED)
    private val executions = Channel<Execution>(Channel.UNLIMITED)

    fun schedule(schedule: ExecutionSchedule) {
        remove(schedule.recordId)
        queue.add(TimedExecutionSchedule(timeProvider.now(), schedule))
        queue.sortBy { it.executeAt }
        reschedule.trySend(Unit)
    }

    fun remove(recordId: String) {
        val removed = queue.removeIf { it.schedule.recordId == recordId }
        if (removed) {
            reschedule.trySend(Unit)
        }
    }

    /**
     * Returns a flow of execution updates.
     * After scheduling a new execution, the flow will emit the states as they are being crawled.
     * Should be called only once.
     */
    fun subscribe(): Flow<Execution> = executions.consumeAsFlow()

    /**
     * Should be called only once.
     * Starts the executor loop.
     * The loop will wait for the next event to be executed and then crawl it.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun run() {
        while (coroutineContext.isActive) {
            select {
                reschedule.onReceive {} // restart the waiting in case something new was scheduled
                onTimeout(untilNextEvent(timeProvider.now())) {
                    val event = queue.removeFirstOrNull()
                    if (event != null) {
                        val execution = crawler.crawl(event.schedule)
                        executions.send(execution)
                    }
                }
            }
        }
    }

    private fun untilNextEvent(now: Instant): Duration {
        val nextEvent = queue.firstOrNull()?.executeAt ?: return Duration.INFINITE
        return Duration.between(now.toJavaInstant(), nextEvent.toJavaInstant())
    }

    private data class TimedExecutionSchedule(
        val executeAt: Instant,
        val schedule: ExecutionSchedule,
    )
}

