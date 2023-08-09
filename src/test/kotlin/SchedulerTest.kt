

import TestHelper.mockTimeProvider
import TestHelper.timestamp
import application.Scheduler
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Event
import org.junit.jupiter.api.Test

class SchedulerTest {
    @Test
    fun `scheduler should wait until next execution`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<Int>(timeProvider)

        val event = Event(timestamp.plusSeconds(1), 1)

        // when
        scheduler.schedule(event)

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events, hasElement(1).and(hasSize(equalTo(1))))
    }

    @Test
    fun `scheduler should be able to inject events afterwards`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<Int>(timeProvider)

        val event = Event(timestamp.plusSeconds(1), 1)

        // when
        launch {
            delay(500)
            scheduler.schedule(event)
        }

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events, hasElement(1).and(hasSize(equalTo(1))))
    }

    @Test
    fun `scheduler should be able to remove events after scheduling before execution`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<String>(timeProvider)

        val removedPayload = "removed"
        val notRemovedPayload = "not-removed"

        val eventToBeRemoved = Event(timestamp.plusSeconds(1), removedPayload)
        val eventToNotBeRemoved = Event(timestamp.plusSeconds(1), notRemovedPayload)

        // when
        scheduler.schedule(eventToBeRemoved)
        scheduler.schedule(eventToNotBeRemoved)
        launch {
            delay(500)
            scheduler.remove(removedPayload)
        }

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events, hasElement(notRemovedPayload).and(hasSize(equalTo(1))))
    }

    @Test
    fun `scheduler returns events based on specified event time`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<Int>(timeProvider)

        val first = Event(timestamp.plusSeconds(1), 1)
        val second = Event(timestamp.plusSeconds(2), 2)

        // when
        launch {
            delay(500)
            scheduler.schedule(first)
        }
        scheduler.schedule(second)

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events, hasSize(equalTo(2)))
        assertThat(events[0], equalTo(first.payload))
        assertThat(events[1], equalTo(second.payload))
    }
}
