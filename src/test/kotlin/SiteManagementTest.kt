
import TestHelper.website
import application.DataRepository
import application.LocalDataRepository
import application.app
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.hamkrest.hasStatus
import org.http4k.testing.ApprovalTest
import org.http4k.testing.Approver
import org.http4k.testing.hasApprovedContent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

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
    fun `client should be able to show all records`(approver: Approver) {
        // given
        // - there is a website in the repository
        val request = Request(Method.GET, "/records")
        repository.add(website)

        // when
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.OK).and(approver.hasApprovedContent()))
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
        assertThat(repository.getAll(), hasElement(website))
    }

    @Test
    fun `client should be able to modify the record`() {
        // given
        val modifiedWebsite = website.copy(label = "test")
        val request = Request(Method.PUT, "/record").with(
            Body.json().toLens() of modifiedWebsite.asJsonObject()
        )

        // when
        // - original website is present in the repository
        repository.add(website)
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.ACCEPTED))
        assertThat(repository.getAll(), hasElement(modifiedWebsite).and(hasSize(equalTo(1))))
    }

    @Test
    fun `client should not be able to modify non existing record`() {
        // given
        val modifiedWebsite = website.copy(id = 2, url = "www.example.com")
        val request = Request(Method.PUT, "/record").with(
            Body.json().toLens() of modifiedWebsite.asJsonObject()
        )

        // when
        // - original website is present in the repository
        repository.add(website)
        val response = application(request)

        // then
        // - repository remains unchanged
        assertThat(response, hasStatus(Status.BAD_REQUEST))
        assertThat(repository.getAll(), hasElement(website).and(hasSize(equalTo(1))))
    }

    @Test
    fun `client should be able to delete existing record`() {
        // given
        val request = Request(Method.DELETE, "/record").with(
            Body.json().toLens() of website.asJsonObject()
        )

        // when
        // - original website is present in the repository
        repository.add(website)
        val response = application(request)

        // then
        assertThat(response, hasStatus(Status.ACCEPTED))
        assertThat(repository.getAll(), hasElement(website).not().and(isEmpty))
    }

    @Test
    fun `client should not be able to delete not existing record`() {
        // given
        val modifiedWebsite = website.copy(id = 2, url = "www.example.com")
        val request = Request(Method.DELETE, "/record").with(
            Body.json().toLens() of modifiedWebsite.asJsonObject()
        )

        // when
        // - original website is present in the repository
        repository.add(website)
        val response = application(request)

        // then
        // - repository remains unchanged
        assertThat(response, hasStatus(Status.BAD_REQUEST))
        assertThat(repository.getAll(), hasElement(website).and(hasSize(equalTo(1))))
    }
}
