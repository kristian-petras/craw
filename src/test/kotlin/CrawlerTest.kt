//
//import application.Crawler
//import io.ktor.client.*
//import io.ktor.client.engine.mock.*
//import io.ktor.http.*
//import kotlinx.coroutines.test.runTest
//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.tuple
//import org.junit.jupiter.api.Test
//
//internal class CrawlerTest {
//
//    @Test
//    fun `crawler should crawl pages recursively`() = runTest {
//        // given
//        val crawler = createCrawler()
//
//        // when
//        val records = crawler.recursiveCrawl("/1", ".*")
//
//        // then
//        assertThat(records)
//            .extracting("url", "links")
//            .containsExactly(
//                tuple("/1", listOf("/2")),
//                tuple("/2", listOf("/3", "/4")),
//                tuple("/3", emptyList<String>()),
//                tuple("/4", emptyList<String>())
//            )
//    }
//
//
//    @Test
//    fun `crawler is able to gather hyperlinks matched by regex`() = runTest {
//        // given
//        val crawler = createCrawler()
//
//        // when
//        val result = crawler.crawl("/5", ".*\\.org$")
//
//        // then
//        assertThat(result.matchedLinks).containsExactly("https://www.wikipedia.org")
//    }
//
//    @Test
//    fun `crawler does not crash on site without matches`() = runTest {
//        // given
//        val crawler = createCrawler()
//
//        // when
//        val result = crawler.crawl("/5", ".*\\.cz$")
//
//        // then
//        assertThat(result.matchedLinks).isEmpty()
//    }
//
//    @Test
//    fun `crawler is able to gather`() = runTest {
//        // given
//        val crawler = createCrawler()
//
//        // when
//        crawler.crawl("/5", ".*")
//        val result = crawler.crawl("/5", ".*\\.cz$")
//
//        // then
//        assertThat(result.url).isEqualTo("/5")
//        assertThat(result.title).isEqualTo("Random Links")
//        assertThat(result.matchedLinks).isEmpty()
//        assertThat(result.links).containsExactly(
//            "https://www.example.com",
//            "https://www.google.com",
//            "https://www.openai.com",
//            "https://www.github.com",
//            "https://www.wikipedia.org"
//        )
//    }
//
//    private fun createCrawler(): Crawler {
//        val client = createFakeClient()
//        return Crawler(client)
//    }
//
//    private fun createFakeClient() = HttpClient(MockEngine) {
//        engine {
//            addHandler {
//                val app = mapOf(site1, site2, site3, site4, site5)
//                val html = app[it.url.fullPath]!!
//                respond(html)
//            }
//        }
//    }
//
//    companion object {
//        val site1 = "/1" to """
//            <title>1</title>
//            <a href="/2">Next site</a>
//        """.trimIndent()
//        val site2 = "/2" to """
//            <title>2</title>
//            <a href="/3">Next site</a>
//            <a href="/4">Next site</a>
//        """.trimIndent()
//        val site3 = "/3" to "<title>3</title>"
//        val site4 = "/4" to "<title>4</title>"
//        val site5 = "/5" to """
//        <!DOCTYPE html>
//        <html>
//        <head>
//            <title>Random Links</title>
//        </head>
//        <body>
//            <h1>Random Links</h1>
//            <p>Check out these random links:</p>
//            <ul>
//                <li><a href="https://www.example.com">Example Website</a></li>
//                <li><a href="https://www.google.com">Google</a></li>
//                <li><a href="https://www.openai.com">OpenAI</a></li>
//                <li><a href="https://www.github.com">GitHub</a></li>
//                <li><a href="https://www.wikipedia.org">Wikipedia</a></li>
//            </ul>
//            <p>Feel free to explore!</p>
//        </body>
//        </html>
//        """.trimIndent()
//    }
//}