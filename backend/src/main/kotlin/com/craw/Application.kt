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
import io.ktor.client.HttpClient
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

suspend fun main(): Unit =
    coroutineScope {
        val sseTranslator = SseTranslator()
        val graphQLTranslator = GraphQLTranslator()
        val restTranslator = RestTranslator()
        val databaseTranslator = DatabaseTranslator()

        val password = System.getenv("POSTGRES_PASSWORD")
        val database = DatabaseFactory.postgres(password)
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
        val graphApplication = GraphApplication(translator = sseTranslator)
        val recordApplication =
            RecordApplication(translator = restTranslator, repository = repository, executor = executor)

        // start executor
        launch {
            executor.subscribe().onEach { graphApplication.update(it) }.collect()
        }

        embeddedServer(
            factory = CIO,
            port = 8080,
            host = "0.0.0.0",
            module = { module(graphQLApplication, graphApplication, recordApplication) },
        ).start(wait = true)
    }
