import application.DataRepository
import application.app
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import model.WebsiteRecord
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.hasApprovedContent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration

internal class FakeDataRepository : DataRepository {
    override fun getWebsiteRecords() = listOf(
        WebsiteRecord(
            url = "www.google.com",
            boundaryRegExp = "",
            periodicity = Duration.ZERO,
            label = "",
            active = true,
            tags = emptyList()
        )
    )
}

@ExtendWith(ApprovalTest::class)
internal class SiteManagementTest {
    private lateinit var application: HttpHandler

    @BeforeEach
    fun setup() {
        application = app(FakeDataRepository())
    }


    @Test
    fun `create new website record`() {
        // given

        // when

        // then
    }

    @Test
    fun `client should be able to show all records`(approver: Approver) {
        // given
        val request = Request(Method.GET, "/records")

        // when
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.OK).and(approver.hasApprovedContent()))
    }

    @Test
    fun `update`() = 3

    @Test
    fun `delete`() = 3
}
