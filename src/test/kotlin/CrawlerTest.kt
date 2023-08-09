
import application.Crawler
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import kotlinx.coroutines.test.runTest
import model.CrawledRecord
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class CrawlerTest {

    private lateinit var crawler: Crawler

    @BeforeEach
    fun setup() {
        val app = routes(
            recursiveUrl bind Method.GET to {
                Response(OK).body(
                    """
                <title>1</title>
                <a href="2">Next site</a>
            """.trimIndent()
                )
            },
            "2" bind Method.GET to {
                Response(OK).body(
                    """
                <title>2</title>
                <a href="3">Next site</a>
                <a href="4">Next site</a>
            """.trimIndent()
                )
            },
            "3" bind Method.GET to { Response(OK).body("<title>3</title>") },
            "4" bind Method.GET to { Response(OK).body("<title>4</title>") },
            singleUrl bind Method.GET to { Response(OK).body(html) }
        )
        crawler = Crawler(app)
    }

    private val recursiveUrl = "1"
    private val singleUrl = "single-page"

    @Disabled("Fix assertion.")
    @Test
    fun `crawler should crawl pages recursively`() = runTest {
        // given
        val request = Request(Method.GET, recursiveUrl)

        // when
        val records = crawler.recursiveCrawl(request, ".*")

        // then
        assertThat(
            records, allOf(
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("1")),
                        has(CrawledRecord::url, equalTo("1")),
                        has(
                            CrawledRecord::matchedLinks, equalTo(
                                listOf("2")
                            )
                        )
                    )
                ),
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("2")),
                        has(CrawledRecord::url, equalTo("2")),
                        has(
                            CrawledRecord::matchedLinks, equalTo(
                                listOf("3", "4")
                            )
                        )
                    )
                ),
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("3")),
                        has(CrawledRecord::url, equalTo("3")),
                        has(
                            CrawledRecord::matchedLinks, equalTo(emptyList())
                        )
                    )
                ),
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("4")),
                        has(CrawledRecord::url, equalTo("4")),
                        has(
                            CrawledRecord::matchedLinks, equalTo(emptyList())
                        )
                    )
                ),

                )
        )
    }

    @Test
    fun `crawler is able to gather hyperlinks`() {
        // given
        val request = Request(Method.GET, singleUrl)

        // when
        val result = crawler.crawl(request, ".*")

        // then
        assertThat(result.url, equalTo(singleUrl))
        assertThat(
            result.matchedLinks, allOf(
                hasElement("https://www.example.com"),
                hasElement("https://www.google.com"),
                hasElement("https://www.openai.com"),
                hasElement("https://www.github.com"),
                hasElement("https://www.wikipedia.org")
            ).and(hasSize(equalTo(5)))
        )
    }

    @Test
    fun `crawler is able to gather title`() {
        // given
        val request = Request(Method.GET, singleUrl)

        // when
        val result = crawler.crawl(request, ".*")

        // then
        assertThat(result.url, equalTo(singleUrl))
        assertThat(result.title, equalTo("Random Links"))
    }

    @Test
    fun `crawler is able to gather hyperlinks matched by regex`() {
        // given
        val request = Request(Method.GET, singleUrl)

        // when
        val result = crawler.crawl(request, ".*\\.org$")

        // then
        assertThat(result.url, equalTo(singleUrl))
        assertThat(result.matchedLinks, hasElement("https://www.wikipedia.org").and(hasSize(equalTo(1))))
    }

    @Test
    fun `crawler does not crash on site without matches`() {
        // given
        val request = Request(Method.GET, singleUrl)

        // when
        val result = crawler.crawl(request, ".*\\.cz$")

        // then
        assertThat(result.url, equalTo(singleUrl))
        assertThat(result.matchedLinks, isEmpty)
    }

    @Test
    fun `crawler is able to crawl multiple times`() {
        // given
        val request = Request(Method.GET, singleUrl)

        // when
        crawler.crawl(request, ".*")
        val result = crawler.crawl(request, ".*\\.cz$")

        // then
        assertThat(result.url, equalTo(singleUrl))
        assertThat(result.matchedLinks, isEmpty)
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