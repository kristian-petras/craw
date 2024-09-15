package com.craw.application

import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState
import com.craw.utility.TimeProvider
import com.craw.utility.between
import com.craw.utility.logger
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
        recordState: RecordState,
        reschedule: Boolean = false,
    ) {
        logger.info("Scheduling record ${recordState.recordId}")
        val executionId = remove(recordState.recordId)
        if (executionId != null) {
            logger.info("Removing old execution $executionId for record ${recordState.recordId}")
            repository.deleteExecution(executionId)
        }

        val now = timeProvider.now()
        val executeAt = if (reschedule) now + recordState.periodicity else now

        logger.info("Scheduling new execution for ${recordState.recordId} at $executeAt")
        val execution =
            repository.createExecution(
                recordId = recordState.recordId,
                url = recordState.url,
                regexp = recordState.regexp,
                start = executeAt,
            )
        logger.info("Created execution ${execution.executionId} for record ${recordState.recordId}")
        addToQueue(executeAt, execution, recordState)
    }

    fun remove(recordId: String): String? {
        val schedule = queue.find { it.recordState.recordId == recordId } ?: return null
        reschedule.trySend(Unit)
        return schedule.execution.executionId
    }

    private fun addToQueue(
        executeAt: Instant,
        execution: Execution.Pending,
        recordState: RecordState,
    ) {
        val timedSchedule = TimedExecutionSchedule(executeAt, execution, recordState)
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
    fun subscribe(): Flow<RecordState> =
        channelFlow {
            while (isActive) {
                select {
                    reschedule.onReceive {} // restart the waiting in case something new was scheduled
                    onTimeout(untilNextEvent(timeProvider.now())) {
                        val (_, execution, record) =
                            queue.removeFirstOrNull()
                                ?: error("Executor queue is empty and until next event timed out")

                        logger.info("Executing execution ${execution.executionId} for record ${record.recordId}")

                        val startedExecution = repository.startExecution(execution.executionId)
                        send(startedExecution.updateRecordState(record))

                        logger.info("Starting crawl for execution ${startedExecution.executionId}")
                        crawler.crawl(
                            execution = execution,
                            url = record.url,
                            regex = record.regexp.toRegex(),
                        )
                            .map { startedExecution.copy(crawl = it) }
                            .collect { send(it.updateRecordState(record)) }
                        logger.info("Crawl for execution ${startedExecution.executionId} finished")

                        val end = timeProvider.now()
                        val executionCompleted = repository.completeExecution(startedExecution.executionId, end)
                        send(executionCompleted.updateRecordState(record))

                        // reschedule the next event
                        schedule(record, true)
                    }
                }
            }
        }

    private fun Execution.updateRecordState(recordState: RecordState): RecordState {
        val record =
            repository.getRecord(recordState.recordId)
                ?: error("Record ${recordState.recordId} while trying to update graph with execution $executionId")

        val newExecutions =
            if (record.executions.any { it.executionId == executionId }) {
                record.executions.map { if (it.executionId == executionId) this else it }
            } else {
                record.executions + listOf(this)
            }

        return record.copy(executions = newExecutions)
    }

    private fun untilNextEvent(now: Instant): Duration {
        val nextEvent =
            queue.firstOrNull()?.executeAt
                ?: return Duration.INFINITE
                    .also { logger.info("Queue is empty, waiting for infinite duration.") }
        return Duration.between(now, nextEvent)
    }

    private data class TimedExecutionSchedule(
        val executeAt: Instant,
        val execution: Execution.Pending,
        val recordState: RecordState,
    )

    companion object {
        private val logger = logger<Executor>()
    }
}
