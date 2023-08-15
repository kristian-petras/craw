//import TestHelper.mockStaticTimeProvider
//import TestHelper.website
//import application.App
//import application.Executor
//import application.repository.LocalDataRepository
//import application.server
//import com.natpryce.hamkrest.*
//import com.natpryce.hamkrest.assertion.assertThat
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.test.runTest
//import okhttp3.OkHttpClient
//import org.http4k.core.*
//import org.http4k.format.KotlinxSerialization.asJsonObject
//import org.http4k.format.KotlinxSerialization.json
//import org.http4k.hamkrest.hasStatus
//import org.http4k.testing.ApprovalTest
//import org.http4k.testing.Approver
//import org.http4k.testing.hasApprovedContent
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//
//internal class IntegrationTest {
//    private lateinit var handler: OkHttpClient
//    private lateinit var client: App.Client
//    private lateinit var app: App
//
//    @BeforeEach
//    fun setup() {
//        val executor = Executor(mockStaticTimeProvider()) { Response(Status.OK) }
//        val repository = LocalDataRepository()
//        app = App(executor, repository)
//        client = app.getClient()
//        handler = server(client)
//    }
//
//    @Test
//    fun `client should be able to show all records`(approver: Approver) = runTest {
//        // given
//        // - app is running
//        val job = launch { app.run() }
//        // - there is a website in the repository
//        val request = Request(Method.GET, "/records")
//        client.add(website)
//
//        // when
//        val response = handler(request)
//
//        // then
//        assertThat(response, hasStatus(Status.OK).and(approver.hasApprovedContent()))
//        job.cancel()
//    }
//
//    @Test
//    fun `client should be able to create a new record`() = runTest {
//        // given
//        // - app is running
//        launch { app.run() }
//        val request = Request(Method.POST, "/record").with(
//            Body.json().toLens() of website.asJsonObject()
//        )
//
//        // when
//        val response = handler(request)
//
//        // then
//        assertThat(response, hasStatus(Status.ACCEPTED))
//        assertThat(client.getAll(), hasElement(website))
//    }
//
//    @Test
//    fun `client should be able to modify the record`() = runTest {
//        // given
//        // - app is running
//        launch { app.run() }
//        val modifiedWebsite = website.copy(label = "test")
//        val request = Request(Method.PUT, "/record").with(
//            Body.json().toLens() of modifiedWebsite.asJsonObject()
//        )
//
//        // when
//        // - original website is present in the repository
//        client.add(website)
//        val response = handler(request)
//
//        // then
//        assertThat(response, hasStatus(Status.ACCEPTED))
//        assertThat(client.getAll(), hasElement(modifiedWebsite).and(hasSize(equalTo(1))))
//    }
//
//    @Test
//    fun `client should not be able to modify non existing record`() = runTest {
//        // given
//        // - app is running
//        launch { app.run() }
//        val modifiedWebsite = website.copy(id = 2, url = "www.example.com")
//        val request = Request(Method.PUT, "/record").with(
//            Body.json().toLens() of modifiedWebsite.asJsonObject()
//        )
//
//        // when
//        // - original website is present in the repository
//        client.add(website)
//        val response = handler(request)
//
//        // then
//        // - repository remains unchanged
//        assertThat(response, hasStatus(Status.BAD_REQUEST))
//        assertThat(client.getAll(), hasElement(website).and(hasSize(equalTo(1))))
//    }
//
//    @Test
//    fun `client should be able to delete existing record`() = runTest {
//        // given
//        // - app is running
//        launch { app.run() }
//        val request = Request(Method.DELETE, "/record").with(
//            Body.json().toLens() of website.asJsonObject()
//        )
//
//        // when
//        // - original website is present in the repository
//        client.add(website)
//        val response = handler(request)
//
//        // then
//        assertThat(response, hasStatus(Status.ACCEPTED))
//        assertThat(client.getAll(), hasElement(website).not().and(isEmpty))
//    }
//
//    @Test
//    fun `client should not be able to delete not existing record`() = runTest {
//        // given
//        // - app is running
//        launch { app.run() }
//        val modifiedWebsite = website.copy(id = 2, url = "www.example.com")
//        val request = Request(Method.DELETE, "/record").with(
//            Body.json().toLens() of modifiedWebsite.asJsonObject()
//        )
//
//        // when
//        // - original website is present in the repository
//        client.add(website)
//        val response = handler(request)
//
//        // then
//        // - repository remains unchanged
//        assertThat(response, hasStatus(Status.BAD_REQUEST))
//        assertThat(client.getAll(), hasElement(website).and(hasSize(equalTo(1))))
//    }
//}
