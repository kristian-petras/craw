
import application.Crawler
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.testing.ApprovalTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
internal class CrawlerTest {

    private lateinit var crawler: Crawler
    @BeforeEach
    fun setup() {
        val app = { _: Request -> Response(OK).body(html) }
        crawler = Crawler(app)
    }

    @Test
    fun `crawler is able to gather hyperlinks`() {
        // given
        val requestUrl = "https://www.example.com"
        val request = Request(Method.GET, requestUrl)

        // when
        val result = crawler.crawl(request, ".*")

        // then
        assertThat(result.url, equalTo(requestUrl))
        assertThat(result.links, allOf(
            hasElement("https://www.example.com"),
            hasElement("https://www.google.com"),
            hasElement("https://www.openai.com"),
            hasElement("https://www.github.com"),
            hasElement("https://www.wikipedia.org")
        ).and(hasSize(equalTo(5))))
    }

    @Test
    fun `crawler is able to gather title`() {
        // given
        val requestUrl = "https://www.example.com"
        val request = Request(Method.GET, requestUrl)

        // when
        val result = crawler.crawl(request, ".*")

        // then
        assertThat(result.url, equalTo(requestUrl))
        assertThat(result.title, equalTo("Random Links"))
    }

    @Test
    fun `crawler is able to gather hyperlinks matched by regex`() {
        // given
        val requestUrl = "https://www.example.com"
        val request = Request(Method.GET, requestUrl)

        // when
        val result = crawler.crawl(request, ".*\\.org$")

        // then
        assertThat(result.url, equalTo(requestUrl))
        assertThat(result.links, hasElement("https://www.wikipedia.org").and(hasSize(equalTo(1))))
    }

    @Test
    fun `crawler does not crash on site without matches`() {
        // given
        val requestUrl = "https://www.example.com"
        val request = Request(Method.GET, requestUrl)

        // when
        val result = crawler.crawl(request, ".*\\.cz$")

        // then
        assertThat(result.url, equalTo(requestUrl))
        assertThat(result.links, isEmpty)
    }

    @Test
    fun `crawler is able to crawl multiple times`() {
        // given
        val requestUrl = "https://www.example.com"
        val request = Request(Method.GET, requestUrl)

        // when
        crawler.crawl(request, ".*")
        val result = crawler.crawl(request, ".*\\.cz$")

        // then
        assertThat(result.url, equalTo(requestUrl))
        assertThat(result.links, isEmpty)
    }

    private val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Random Links</title>
        </head>
        <body>
            <h1>Random Links</h1>
            <p>Check out these random links:</p>
            <ul>
                <li><a href="https://www.example.com">Example Website</a></li>
                <li><a href="https://www.google.com">Google</a></li>
                <li><a href="https://www.openai.com">OpenAI</a></li>
                <li><a href="https://www.github.com">GitHub</a></li>
                <li><a href="https://www.wikipedia.org">Wikipedia</a></li>
            </ul>
            <p>Feel free to explore!</p>
        </body>
        </html>
        """.trimIndent()
}