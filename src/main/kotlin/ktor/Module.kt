package ktor

import application.App
import application.Executor
import application.repository.LocalDataRepository
import application.repository.MongoDataRepository
import graphql.graphQL
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.coroutines.launch
import sse.SseApp
import sse.sse
import utility.TimeProvider
import java.time.Instant

fun Application.module() {
    val timeProvider = TimeProvider { Instant.now() }
    val env = environment.config.propertyOrNull("ktor.environment")?.getString()
    val repository = when (env) {
        "dev" -> MongoDataRepository(System.getenv("MONGO_DB_CONNECTION_STRING"))
        "test" -> LocalDataRepository()
        else -> LocalDataRepository()
    }

    val executor = Executor(timeProvider)
    val app = App(executor, repository, timeProvider)
    configureSerialization()
    configureRouting(app.getClient())
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    sse(SseApp())
    graphQL()
    launch { app.run() }
}