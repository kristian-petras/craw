import application.TimeProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import model.WebsiteRecord
import java.time.Instant
import kotlin.time.Duration

object TestHelper {
    val website =
        WebsiteRecord(
            url = "www.google.com",
            boundaryRegExp = "",
            periodicity = Duration.ZERO,
            label = "",
            active = true,
            tags = emptyList(),
            executionStatus = false
        )

    val timestamp: Instant = Instant.parse("2023-07-01T10:00:00Z")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun TestScope.mockTimeProvider(now: Instant = timestamp): TimeProvider {
        val timeProvider = mockk<TimeProvider> { Instant.now() }
        every { timeProvider.now() } answers { now.plusMillis(currentTime) }
        return timeProvider
    }
}