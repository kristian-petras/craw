package application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.between
import java.time.Instant
import java.util.*
import kotlin.time.Duration

class Scheduler<T>(private val timeProvider: TimeProvider) {
    private companion object {
        val logger: Logger = LoggerFactory.getLogger(Scheduler::class.java)
    }

    private val compareByTimestamp: Comparator<Event<T>> = compareBy { it.timestamp }
    private val eventQueue = PriorityQueue(compareByTimestamp)
    private val eventChannel = Channel<Event<T>>(capacity = Channel.UNLIMITED)

    suspend fun schedule(event: Event<T>) = eventChannel.send(event)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribe() : Flow<Event<T>> = flow {
        while (true) {
            select {
                eventChannel.onReceive {
                    logger.info("Scheduling event: $it")
                    eventQueue.add(it)
                }
                onTimeout(durationUntilNextEvent()) {
                    val event = eventQueue.poll()
                        ?: error("Update flow timeout should wait indefinitely until an event is injected.")
                    logger.info("Emitting event $event")
                    emit(event)
                }
            }
        }
    }
    private fun durationUntilNextEvent(): Duration {
        val event = eventQueue.peek()?.timestamp ?: Instant.MAX
        return Duration.between(timeProvider.now(), event)
    }
}