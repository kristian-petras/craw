

import application.Event
import application.Scheduler
import application.TimeProvider
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerTest {
    private companion object {
        val timestamp: Instant = Instant.parse("2023-07-01T10:00:00Z")
    }
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

        assertThat(events, hasElement(event).and(hasSize(equalTo(1))))
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

        assertThat(events, hasElement(event).and(hasSize(equalTo(1))))
    }

    private fun TestScope.mockTimeProvider(): TimeProvider {
        val timeProvider = mockk<TimeProvider> { Instant.now() }
        every { timeProvider.now() } answers { timestamp.plusMillis(currentTime) }
        return timeProvider
    }
}
