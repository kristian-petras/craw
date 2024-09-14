package com.craw

import com.craw.application.Crawler
import com.craw.application.Executor
import com.craw.application.GraphApplication
import com.craw.application.GraphQLApplication
import com.craw.application.Parser
import com.craw.application.RecordApplication
import com.craw.application.Repository
import com.craw.ktor.module
import com.craw.translator.DatabaseTranslator
import com.craw.translator.GraphQLTranslator
import com.craw.translator.RestTranslator
import com.craw.translator.SseTranslator
import com.craw.utility.DatabaseFactory
import com.craw.utility.TimeProvider
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock

fun main() {
    val sseTranslator = SseTranslator()
    val graphQLTranslator = GraphQLTranslator()
    val restTranslator = RestTranslator()
    val databaseTranslator = DatabaseTranslator()

    val environment = dotenv()
    val database = DatabaseFactory.postgres(environment["POSTGRES_PASSWORD"]!!)
    val repository = Repository(translator = databaseTranslator, database = database)

    val timeProvider = TimeProvider { Clock.System.now() }
    val parser = Parser()
    val crawler =
        Crawler(
            timeProvider = timeProvider,
            repository = repository,
            client = HttpClient(),
            parser = parser,
            dispatcher = Dispatchers.IO,
        )
    val executor = Executor(timeProvider = timeProvider, crawler = crawler, repository = repository)

    val graphQLApplication = GraphQLApplication(translator = graphQLTranslator, repository = repository)
    val graphApplication = GraphApplication(translator = sseTranslator, executor = executor)
    val recordApplication = RecordApplication(translator = restTranslator, repository = repository, executor = executor)

    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = { module(graphQLApplication, graphApplication, recordApplication) },
    ).start(wait = true)
}
