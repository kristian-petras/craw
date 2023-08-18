package ktor

import application.App
import application.Executor
import application.repository.LocalDataRepository
import application.repository.MongoDataRepository
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import utility.TimeProvider
import java.time.Instant

fun Application.module() {
    val timeProvider = TimeProvider { Instant.now() }
    val env = environment.config.propertyOrNull("ktor.environment")?.getString()
    val repository = when (env) {
        "dev" -> MongoDataRepository(System.getenv("MONGO_DB_CONNECTION_STRING"))
        "test" -> LocalDataRepository()
        else -> TODO()
    }

    val executor = Executor(timeProvider)
    val app = App(executor, repository, timeProvider)
    configureSerialization()
    configureRouting(app.getClient())
    launch { app.run() }
}