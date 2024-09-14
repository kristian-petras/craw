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
                url = recordState.baseUrl,
                regexp = recordState.regexp,
                start = executeAt,
            )
        logger.info("Created execution ${execution.executionId} for record ${recordState.recordId}")
        addToQueue(executeAt, execution.executionId, recordState)
    }

    fun remove(recordId: String): String? {
        val execution = queue.find { it.recordState.recordId == recordId } ?: return null
        reschedule.trySend(Unit)
        return execution.executionId
    }

    private fun addToQueue(
        executeAt: Instant,
        executionId: String,
        recordState: RecordState,
    ) {
        val timedSchedule = TimedExecutionSchedule(executeAt, executionId, recordState)
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
                        val (_, executionId, record) =
                            queue.firstOrNull()
                                ?: error("Executor queue is empty and until next event timed out")

                        logger.info("Executing execution $executionId for record ${record.recordId}")

                        val execution = repository.startExecution(executionId)
                        send(execution.updateRecordState(record))

                        logger.info("Starting crawl for execution $executionId")
                        crawler.crawl(
                            executionId = execution.executionId,
                            url = record.baseUrl,
                            regex = record.regexp.toRegex(),
                        )
                            .map { execution.copy(crawl = it) }
                            .collect { send(it.updateRecordState(record)) }
                        logger.info("Crawl for execution $executionId finished")

                        val end = timeProvider.now()
                        val executionCompleted = repository.completeExecution(execution.executionId, end)
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
        val nextEvent = queue.firstOrNull()?.executeAt ?: return Duration.INFINITE
        return Duration.between(now, nextEvent)
    }

    private data class TimedExecutionSchedule(
        val executeAt: Instant,
        val executionId: String,
        val recordState: RecordState,
    )

    companion object {
        private val logger = logger<Executor>()
    }
}
