package com.craw.app.executor

import com.craw.application.Crawler
import com.craw.application.Parser
import com.craw.application.Repository
import com.craw.schema.internal.Crawl
import com.craw.utility.TimeProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CrawlerTest {
    @Test
    fun x() =
        runTest {
            // given
            val timeProvider = mockTimeProvider()

            val id = slot<String>()
            val start = slot<Instant>()

            val repository =
                mockk<Repository> {
//                    every { activateCrawl(capture(id), capture(start)) } answers {
//                        Crawl.Running(
//                            crawlId = id.captured,
//                            url = "root",
//                            start = start.captured,
//                        )
//                    }
                }

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = ByteReadChannel(request.url.toString()),
                        status = HttpStatusCode.OK,
                        headers = Headers.Empty,
                    )
                }
            val client = HttpClient(mockEngine)

            val parser =
                mockk<Parser> {
                }

            val url = "root"

            val crawler =
                Crawler(
                    timeProvider = timeProvider,
                    repository = repository,
                    client = client,
                    parser = parser,
                    dispatcher = StandardTestDispatcher(this.testScheduler),
                )

            // when
            val crawls = crawler.crawl("1", url, Regex(".*")).toList()

            // then
            assertThat(crawls).containsExactly(
                Crawl.Running("1", url, timeProvider.now()),
            )
        }

    private fun TestScope.mockTimeProvider(): TimeProvider =
        mockk {
            every { now() } answers { Instant.parse("2021-01-01T00:00:00Z").plus(currentTime.milliseconds) }
        }
}
