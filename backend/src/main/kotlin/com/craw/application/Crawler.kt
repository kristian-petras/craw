package com.craw.application

import com.craw.application.Parser.ParseResult
import com.craw.schema.internal.Crawl
import com.craw.utility.TimeProvider
import com.craw.utility.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class Crawler(
    private val timeProvider: TimeProvider,
    private val repository: Repository,
    private val parser: Parser,
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) {
    private val updateChannel = Channel<Unit>(Channel.UNLIMITED)
    // TODO: website cache with expiration

    fun crawl(
        executionId: String,
        url: String,
        regex: Regex,
    ): Flow<Crawl> =
        channelFlow {
            val root = repository.createCrawl(executionId, url, null)
            updateChannel.send(Unit)
            logger.info("[${root.crawlId}] Created root crawl for $url and executionId $executionId with regex $regex")

            val sendJob =
                async {
                    updateChannel
                        .consumeAsFlow()
                        .mapNotNull { repository.getCrawl(root.crawlId) }
                        .onEach { logger.info("[${it.crawlId}] Sending updated state to re-render") }
                        .collect(::send)
                }

            process(
                scope = this,
                executionId = executionId,
                parentId = root.crawlId,
                crawl = root,
                regex = regex,
            )

            logger.info("[${root.crawlId}] Root crawl finished")
            updateChannel.close()
            sendJob.await()
        }

    private suspend fun process(
        scope: CoroutineScope,
        executionId: String,
        parentId: String,
        crawl: Crawl.Pending,
        regex: Regex,
    ) {
        val started = repository.startCrawl(crawl.crawlId, timeProvider.now())
        logger.info("[${started.crawlId}] Starting crawl for ${started.url} at ${started.start}")
        updateChannel.send(Unit)

        when (val fetched = fetchLinks(crawl.url, regex)) {
            is ParseResult.Failure -> {
                repository.invalidateCrawl(
                    crawlId = crawl.crawlId,
                    error = fetched.message,
                )
                updateChannel.send(Unit)
                return
            }

            is ParseResult.Success -> {
                val (title, matches, rest) = fetched
                logger.info("[${started.crawlId}] Found ${matches.size} matches and ${rest.size} invalid links")

                val pending =
                    matches.map { repository.createCrawl(executionId, it, parentId) }.onEach {
                        logger.info("[${it.crawlId}] Created crawl for ${it.url} with parent $parentId")
                    }
                rest.map { repository.createInvalidCrawl(executionId, it, parentId) }.onEach {
                    logger.info("[${it.crawlId}] Created invalid crawl for ${it.url} with parent $parentId")
                }

                val completed =
                    repository.completeCrawl(
                        crawlId = crawl.crawlId,
                        title = title,
                        end = timeProvider.now(),
                    )
                logger.info("[${completed.crawlId}] Completed crawl for ${completed.url} at ${completed.end}")
                updateChannel.send(Unit)

                pending.map { child ->
                    scope.async(dispatcher) {
                        process(
                            scope = scope,
                            executionId = executionId,
                            parentId = completed.crawlId,
                            crawl = child,
                            regex = regex,
                        )
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun fetchLinks(
        url: String,
        regex: Regex,
    ): ParseResult {
        // TODO: robots.txt handling
        val payload = client.get(url)
        if (payload.status.isSuccess()) {
            logger.info("Fetched $url with status ${payload.status}")
            return parser.parse(payload.bodyAsText(), regex)
        } else {
            logger.error("Failed to fetch $url with status ${payload.status}")
            return ParseResult.Failure(payload.status.value, payload.status.description)
        }
    }

    companion object {
        private val logger = logger<Crawler>()
    }
}
