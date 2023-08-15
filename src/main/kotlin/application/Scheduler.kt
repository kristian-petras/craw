package application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import model.Event
import model.ExecutorSearch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.TimeProvider
import utility.between
import java.time.Instant
import java.util.*
import kotlin.time.Duration

class Scheduler(private val timeProvider: TimeProvider) {
    private companion object {
        val logger: Logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    private val compareByTimestamp: Comparator<Event<ExecutorSearch>> = compareBy { it.timestamp }
    private val eventQueue = PriorityQueue(compareByTimestamp)
    private val eventChannel = Channel<Event<ExecutorSearch>>(capacity = Channel.UNLIMITED)

    fun schedule(event: Event<ExecutorSearch>) {
        eventChannel.trySendBlocking(event)
            .onSuccess { logger.info("Scheduled event $event") }
            .onFailure { throw IllegalStateException("Could not schedule event $event", it) }
            .onClosed { throw IllegalStateException("Event channel was closed", it) }
    }

    fun remove(recordId: Int): Boolean =
        eventQueue.removeIf { it.payload.id == recordId }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe(): Flow<ExecutorSearch> = flow {
        while (true) {
            select {
                eventChannel.onReceive {
                    eventQueue.add(it)
                }
                onTimeout(durationUntilNextEvent()) {
                    val event = eventQueue.poll()
                        ?: error("Update flow timeout should wait indefinitely until an event is injected.")
                    logger.info("Emitting event $event")
                    emit(event.payload)
                }
            }
        }
    }

    private fun durationUntilNextEvent(): Duration {
        val event = eventQueue.peek()?.timestamp ?: Instant.MAX
        return Duration.between(timeProvider.now(), event)
    }
}