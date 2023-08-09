
import application.Executor
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import kotlinx.coroutines.test.runTest
import model.CrawledRecord
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.testing.ApprovalTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ApprovalTest::class)
internal class ExecutorTest {
    private lateinit var executor: Executor

    @BeforeEach
    fun setup() {
        val app = routes(
            "1" bind GET to {
                Response(OK).body(
                    """
                <title>1</title>
                <a href="2">Next site</a>
            """.trimIndent()
                )
            },
            "2" bind GET to {
                Response(OK).body(
                    """
                <title>2</title>
                <a href="3">Next site</a>
                <a href="4">Next site</a>
            """.trimIndent()
                )
            },
            "3" bind GET to { Response(OK).body("<title>3</title>") },
            "4" bind GET to { Response(OK).body("<title>4</title>") },
        )
        executor = Executor(app)
    }

    @Disabled("Fix assertion.")
    @Test
    fun `executor should crawl pages recursively`() = runTest {
        // given
        val request = Request(GET, "1")

        // when
        val records = executor.crawl(request, ".*")

        // then
        assertThat(
            records, allOf(
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("1")),
                        has(CrawledRecord::url, equalTo("1")),
                        has(
                            CrawledRecord::links, equalTo(
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
                            CrawledRecord::links, equalTo(
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
                            CrawledRecord::links, equalTo(emptyList())
                        )
                    )
                ),
                hasElement(
                    allOf(
                        has(CrawledRecord::title, equalTo("4")),
                        has(CrawledRecord::url, equalTo("4")),
                        has(
                            CrawledRecord::links, equalTo(emptyList())
                        )
                    )
                ),

                )
        )
    }

    @Test
    fun `executor should execute scheduled crawling`() {

    }
}