

import TestHelper.mockTimeProvider
import TestHelper.timestamp
import application.Scheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Event
import model.ExecutorSearch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchedulerTest {
    @Test
    fun `scheduler should wait until next execution`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler(timeProvider)

        val search = ExecutorSearch(1, "", "")
        val event = Event(timestamp.plusSeconds(1), search)

        // when
        scheduler.schedule(event)

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events).containsExactly(search)
    }

    @Test
    fun `scheduler should be able to inject events afterwards`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler(timeProvider)

        val search = ExecutorSearch(1, "", "")
        val event = Event(timestamp.plusSeconds(1), search)

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

        assertThat(events).containsExactly(search)
    }

    @Test
    fun `scheduler should be able to remove events after scheduling before execution`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler(timeProvider)

        val removed = ExecutorSearch(1, "removed", "")
        val notRemoved = ExecutorSearch(2, "not-removed", "")

        val eventToBeRemoved = Event(timestamp.plusSeconds(1), removed)
        val eventToNotBeRemoved = Event(timestamp.plusSeconds(1), notRemoved)

        // when
        scheduler.schedule(eventToBeRemoved)
        scheduler.schedule(eventToNotBeRemoved)
        launch {
            delay(500)
            scheduler.remove(removed.id)
        }

        // then
        val events = scheduler
            .subscribe()
            .catch { }
            .toList()

        assertThat(events).containsExactly(notRemoved)
    }

    @Test
    fun `scheduler returns events based on specified event time`() = runTest {
        // given
        val timeProvider = mockTimeProvider()
        val scheduler = Scheduler(timeProvider)

        val first = Event(timestamp.plusSeconds(1), ExecutorSearch(1, "", ""))
        val second = Event(timestamp.plusSeconds(2), ExecutorSearch(2, "", ""))

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

        assertThat(events).containsExactly(first.payload, second.payload)
    }
}
