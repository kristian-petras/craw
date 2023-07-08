
import application.DataRepository
import application.LocalDataRepository
import application.app
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.hasElement
import model.WebsiteRecord
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.hamkrest.hasStatus
import org.http4k.lens.*
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.hasApprovedContent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration


private val website =
    WebsiteRecord(
        url = "www.google.com",
        boundaryRegExp = "",
        periodicity = Duration.ZERO,
        label = "",
        active = true,
        tags = emptyList()
    )

@ExtendWith(ApprovalTest::class)
internal class SiteManagementTest {
    private lateinit var application: HttpHandler
    private lateinit var repository: DataRepository

    @BeforeEach
    fun setup() {
        repository = LocalDataRepository()
        application = app(repository)
    }


    @Test
    fun `client should be able to create a new record`() {
        // given
        val request = Request(Method.POST, "/record").with(
            Body.json().toLens() of website.asJsonObject()
        )

        // when
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.ACCEPTED))
        assertThat(repository.getWebsiteRecords(), hasElement(website))
    }

    @Test
    fun `client should be able to show all records`(approver: Approver) {
        // given
        val request = Request(Method.GET, "/records")

        // when
        // - adds sample website record to fetch
        repository.addWebsiteRecord(website)
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.OK).and(approver.hasApprovedContent()))
    }

    @Test
    fun `update`() = 3

    @Test
    fun `delete`() = 3
}
