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
    private val delete = Channel<String>(Channel.UNLIMITED)

    fun schedule(
        recordId: String,
        reschedule: Boolean = false,
    ) {
        val recordState = repository.getRecord(recordId)
        if (recordState == null || recordState.active.not()) {
            logger.info("Record $recordId is not active, skipping scheduling.")
            remove(recordId)
            return
        }

        logger.info("Scheduling record ${recordState.recordId}")
        val execution = find(recordState.recordId)?.execution

        if (execution == null) {
            val now = timeProvider.now()
            val executeAt = if (reschedule) now + recordState.periodicity else now

            logger.info("Scheduling new execution for ${recordState.recordId} at $executeAt")
            val newExecution =
                repository.createExecution(
                    recordId = recordState.recordId,
                    url = recordState.url,
                    regexp = recordState.regexp,
                    start = executeAt,
                )
            logger.info("Created execution ${newExecution.executionId} for record ${recordState.recordId}")
            addToQueue(executeAt, newExecution, recordState)
        } else if (recordState.executions.none { it is Execution.Running }) {
            logger.info("Execution ${execution.executionId} for record ${recordState.recordId} is outdated, removing.")
            remove(recordState.recordId)
            schedule(recordState.recordId, reschedule)
        } else {
            logger.info(
                "Execution ${execution.executionId} for record ${recordState.recordId} is outdated," +
                    "but still running. Skipping rescheduling.",
            )
        }
    }

    fun remove(recordId: String): Execution.Pending? {
        delete.trySend(recordId)
        val schedule = find(recordId) ?: return null
        val success = queue.remove(schedule)
        if (success) {
            logger.info("Removed record $recordId from the queue")
        } else {
            logger.info("Record $recordId was not found in the queue")
        }
        reschedule.trySend(Unit)
        return schedule.execution
    }

    private fun find(recordId: String): TimedExecutionSchedule? = queue.find { it.recordState.recordId == recordId }

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

    sealed interface ExecutorOutput {
        data class Record(val record: RecordState) : ExecutorOutput

        data class Remove(val recordId: String) : ExecutorOutput
    }

    /**
     * Returns a flow of execution updates.
     * After scheduling a new execution, the flow will emit the states as they are being crawled.
     * Should be called only once.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe(): Flow<ExecutorOutput> =
        channelFlow {
            while (isActive) {
                select {
                    delete.onReceive {
                        logger.info("Received delete for record $it")
                        send(ExecutorOutput.Remove(it))
                    }
                    reschedule.onReceive {} // restart the waiting in case something new was scheduled
                    onTimeout(untilNextEvent(timeProvider.now())) {
                        val (_, execution, record) =
                            queue.removeFirstOrNull()
                                ?: error("Executor queue is empty and until next event timed out")

                        logger.info("Executing execution ${execution.executionId} for record ${record.recordId}")

                        val startedExecution = repository.startExecution(execution.executionId)
                        send(ExecutorOutput.Record(startedExecution.updateRecordState(record)))

                        logger.info("Starting crawl for execution ${startedExecution.executionId}")
                        crawler.crawl(
                            execution = execution,
                            url = record.url,
                            regex = record.regexp.toRegex(),
                        )
                            .map { startedExecution.copy(crawl = it) }
                            .collect { send(ExecutorOutput.Record(it.updateRecordState(record))) }
                        logger.info("Crawl for execution ${startedExecution.executionId} finished")

                        val end = timeProvider.now()
                        val executionCompleted = repository.completeExecution(startedExecution.executionId, end)
                        send(ExecutorOutput.Record(executionCompleted.updateRecordState(record)))

                        // reschedule the next event
                        schedule(record.recordId, true)
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
