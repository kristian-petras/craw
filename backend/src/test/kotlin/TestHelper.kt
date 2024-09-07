
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import model.WebsiteRecord
import utility.TimeProvider
import java.time.Instant
import kotlin.time.Duration

object TestHelper {
    val website =
        WebsiteRecord(
            id = 1,
            url = "www.google.com",
            boundaryRegExp = "",
            periodicity = Duration.ZERO.toIsoString(),
            label = "",
            active = true,
            tags = emptyList(),
            executions = emptyList(),
            lastExecutionTimestamp = null,
            lastExecutionStatus = null,
        )

    val timestamp: Instant = Instant.parse("2023-07-01T10:00:00Z")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun TestScope.mockTimeProvider(now: Instant = timestamp): TimeProvider =
        mockk<TimeProvider> {
            every { now() } answers { now.plusMillis(currentTime) }
        }

    fun mockStaticTimeProvider(now: Instant = timestamp): TimeProvider =
        mockk<TimeProvider> {
            every { now() } answers { now }
        }
}
