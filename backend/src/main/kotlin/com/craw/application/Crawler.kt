package com.craw.application

import com.craw.schema.database.CrawlType
import com.craw.schema.internal.Crawl
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.datetime.Instant
import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.craw.utility.TimeProvider
import java.net.URI
import kotlin.time.measureTime

class Crawler(
    private val timeProvider: TimeProvider,
    private val repository: Repository,
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) {
    fun crawl(executionId: String, url: String, regex: Regex): Flow<Crawl> = channelFlow {
        val root = repository.create(CrawlCreate(executionId, url, null))

        process(
            executionId = executionId,
            parentId = null,
            crawl = root,
            regex = regex,
            activateCallback = { send(it) },
            completeCallback = { send(it) }
        )
    }

    private suspend fun process(
        executionId: String,
        parentId: String?,
        crawl: Crawl.Pending,
        regex: Regex,
        activateCallback: suspend (Crawl.Running) -> Unit,
        completeCallback: suspend (Crawl.Completed) -> Unit
    ) {
        val activate = repository.activate(CrawlActivate(crawl.crawlId, timeProvider.now()))
        activateCallback(activate)

        val (title, matches, rest) = fetchLinks(crawl.url, regex)

        val pending = matches.map { repository.create(CrawlCreate(executionId, it, parentId)) }
        val invalid = rest.map { repository.invalidate(CrawlNotMatched(executionId, it)) }

        var completed = repository.complete(
            crawlComplete = CrawlComplete(
                crawlId = crawl.crawlId,
                title = title,
                end = timeProvider.now(),
                pending = pending,
                invalid = invalid
            )
        )
        completeCallback(completed)

        pending.forEach { child ->
            process(
                executionId = executionId,
                parentId = completed.crawlId,
                crawl = child,
                regex = regex,
                activateCallback = {
                    completed = repository.update(CrawlUpdateRunning(completed.crawlId, it))
                    completeCallback(completed)
                },
                completeCallback = {
                    completed = repository.update(CrawlUpdate(completed.crawlId, it))
                    completeCallback(completed)
                }
            )
        }
    }

    private data class ParseResult(val title: String?, val matches: List<String>, val rest: List<String>)

    private suspend fun fetchLinks(url: String, regex: Regex): ParseResult {
        val payload = client.get(url).bodyAsText() // TODO handle failure
        val document = Jsoup.parse(payload)
        val title = document.title()
        val links = document.select("a[href]").map { link -> link.attr("href") }
        val (matches, rest) = links.partition { regex.matches(it) }
        return ParseResult(title, matches, rest)
    }

    companion object {
        private val logger = logger<Crawler>()
    }
}

inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)