package com.craw.application

import com.craw.application.Parser.ParseResult
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.utility.TimeProvider
import com.craw.utility.logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.seconds

class Crawler(
    private val timeProvider: TimeProvider,
    private val repository: Repository,
    private val parser: Parser,
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) {
    fun crawl(
        execution: Execution.Pending,
        url: String,
        regex: Regex,
    ): Flow<Crawl> =
        channelFlow {
            val cache = mutableSetOf<String>()
            val updateChannel = Channel<Unit>(Channel.UNLIMITED)
            val root = execution.crawl
            updateChannel.send(Unit)
            logger.info("[${root.crawlId}] Created root crawl for $url and executionId ${execution.executionId} with regex $regex")

            val sendJob =
                async {
                    updateChannel
                        .consumeAsFlow()
                        .mapNotNull { repository.getCrawl(root.crawlId) }
                        .onEach { logger.info("[${it.crawlId}] Sending updated state $it to re-render") }
                        .collect(::send)
                }

            process(
                scope = this,
                executionId = execution.executionId,
                parentId = root.crawlId,
                crawl = root,
                regex = regex,
                updateChannel = updateChannel,
                cache = cache,
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
        updateChannel: SendChannel<Unit>,
        cache: Set<String>,
    ) {
        val started = repository.startCrawl(crawl.crawlId, timeProvider.now())
        logger.info("[${started.crawlId}] Starting crawl for ${started.url} at ${started.start}")
        updateChannel.send(Unit)

        when (val fetched = fetchLinks(crawl.url, regex, cache)) {
            is ParseResult.Failure -> {
                repository.invalidateCrawl(
                    crawlId = crawl.crawlId,
                    error = fetched.message,
                    end = timeProvider.now(),
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
                rest.map {
                    repository.createInvalidCrawl(
                        executionId = executionId,
                        url = it,
                        parentId = parentId,
                        time = timeProvider.now(),
                        error = "Did not match the regex $regex",
                    )
                }.onEach {
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

                val updatedCache = cache + matches + rest
                pending.map { child ->
                    scope.async(dispatcher) {
                        process(
                            scope = scope,
                            executionId = executionId,
                            parentId = completed.crawlId,
                            crawl = child,
                            regex = regex,
                            updateChannel = updateChannel,
                            cache = updatedCache,
                        )
                    }
                }.awaitAll()
            }
        }
    }

    private suspend fun fetchLinks(
        url: Url,
        regex: Regex,
        cache: Set<String>,
    ): ParseResult {
        // TODO: robots.txt handling
        val payload = runCatching { client.get(url) }.getOrNull()
        delay(10.seconds)
        if (payload != null && payload.status.isSuccess()) {
            logger.info("Fetched $url with status ${payload.status}")
            return parser.parse(url, payload.bodyAsText(), regex, cache)
        } else {
            logger.warn("Failed to fetch $url")
            if (payload != null) {
                return ParseResult.Failure(payload.status.value, payload.status.description)
            }
            return ParseResult.Failure(HttpStatusCode.BadRequest.value, "Failed to fetch $url")
        }
    }

    companion object {
        private val logger = logger<Crawler>()
    }
}
