

import TestHelper.mockTimeProvider
import TestHelper.timestamp
import application.Event
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
import org.junit.jupiter.api.Test

class SchedulerTest {
    @Test
    fun `scheduler should wait until next execution`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<Unit>(timeProvider)

        val event = Event(timestamp.plusSeconds(1), Unit)

        // when
        scheduler.schedule(event)

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events, hasElement(Unit).and(hasSize(equalTo(1))))
    }

    @Test
    fun `scheduler should be able to inject events afterwards`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler<Unit>(timeProvider)

        val event = Event(timestamp.plusSeconds(1), Unit)

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

        assertThat(events, hasElement(Unit).and(hasSize(equalTo(1))))
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
