package com.craw

import com.craw.utility.DatabaseFactory
import com.craw.application.Crawler
import com.craw.application.Executor
import com.craw.application.GraphApplication
import com.craw.application.GraphQLApplication
import com.craw.application.RecordApplication
import com.craw.application.Repository
import com.craw.ktor.CrawlerServer
import com.craw.translator.DatabaseTranslator
import com.craw.translator.GraphQLTranslator
import com.craw.translator.RestTranslator
import com.craw.translator.SseTranslator
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import com.craw.utility.TimeProvider

fun main() {
    val sseTranslator = SseTranslator()
    val graphQLTranslator = GraphQLTranslator()
    val restTranslator = RestTranslator()
    val databaseTranslator = DatabaseTranslator()

    val environment = dotenv()
    val database = DatabaseFactory.postgres(environment["POSTGRES_PASSWORD"]!!)
    val repository = Repository(translator = databaseTranslator, database = database)

    val timeProvider = TimeProvider { Clock.System.now() }
    val crawler = Crawler(
        timeProvider = timeProvider,
        repository = repository,
        client = HttpClient(),
        dispatcher = Dispatchers.IO
    )
    val executor = Executor(timeProvider = timeProvider, crawler = crawler, repository = repository)

    val graphQLApplication = GraphQLApplication(translator = graphQLTranslator, repository = repository)
    val graphApplication = GraphApplication(translator = sseTranslator)
    val recordApplication = RecordApplication(translator = restTranslator, repository = repository, executor = executor)

    val server = CrawlerServer(
        graphQLApplication = graphQLApplication,
        graphApplication = graphApplication,
        recordApplication = recordApplication,
        executor = executor,
    )

    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = { with(server) { module() } }
    ).start(wait = true)
}