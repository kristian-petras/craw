package com.craw.application

import com.craw.schema.internal.ExecutionUpdate
import com.craw.utility.TimeProvider
import com.craw.utility.between
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.datetime.Instant
import kotlin.time.Duration

class Executor(
    private val timeProvider: TimeProvider,
    private val crawler: Crawler,
    private val repository: Repository,
) {
    private val queue = mutableListOf<TimedExecutionSchedule>()
    private val reschedule = Channel<Unit>(Channel.UNLIMITED)

    fun schedule(
        schedule: ExecutionSchedule,
        reschedule: Boolean = false,
    ) {
        val executionId = remove(schedule.recordId)
        if (executionId != null) {
            repository.deleteExecution(executionId)
        }

        val now = timeProvider.now()
        val executeAt = if (reschedule) now + schedule.periodicity else now

        val execution =
            repository.createExecution(
                recordId = schedule.recordId,
                url = schedule.url,
                regexp = schedule.regexp.pattern,
                start = executeAt,
            )

        addToQueue(executeAt, execution.executionId, schedule)
    }

    fun remove(recordId: String): String? {
        val execution = queue.find { it.schedule.recordId == recordId } ?: return null
        reschedule.trySend(Unit)
        return execution.executionId
    }

    private fun addToQueue(
        executeAt: Instant,
        executionId: String,
        schedule: ExecutionSchedule,
    ) {
        val timedSchedule = TimedExecutionSchedule(executeAt, executionId, schedule)
        queue.add(timedSchedule)
        queue.sortBy { it.executeAt }
        reschedule.trySend(Unit)
    }

    /**
     * Returns a flow of execution updates.
     * After scheduling a new execution, the flow will emit the states as they are being crawled.
     * Should be called only once.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe(): Flow<ExecutionUpdate> =
        channelFlow {
            while (isActive) {
                select {
                    reschedule.onReceive {} // restart the waiting in case something new was scheduled
                    onTimeout(untilNextEvent(timeProvider.now())) {
                        val (_, executionId, schedule) =
                            queue.firstOrNull()
                                ?: error("Executor queue is empty and until next event timed out")

                        val execution = repository.startExecution(executionId)
                        send(ExecutionUpdate(schedule.recordId, execution))

                        crawler.crawl(
                            executionId = execution.executionId,
                            url = schedule.url,
                            regex = schedule.regexp,
                        )
                            .map { execution.copy(crawl = it) }
                            .collect { send(ExecutionUpdate(schedule.recordId, it)) }

                        val end = timeProvider.now()
                        val executionCompleted = repository.completeExecution(execution.executionId, end)
                        send(ExecutionUpdate(schedule.recordId, executionCompleted))

                        // reschedule the next event
                        schedule(schedule, true)
                    }
                }
            }
        }

    private fun untilNextEvent(now: Instant): Duration {
        val nextEvent = queue.firstOrNull()?.executeAt ?: return Duration.INFINITE
        return Duration.between(now, nextEvent)
    }

    private data class TimedExecutionSchedule(
        val executeAt: Instant,
        val executionId: String,
        val schedule: ExecutionSchedule,
    )
}
