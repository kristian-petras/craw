package ktor

import application.App
import application.Executor
import application.repository.MongoDataRepository
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import utility.TimeProvider
import java.time.Instant

fun Application.module() {
    val timeProvider = TimeProvider { Instant.now() }
    val connectionString = System.getenv("MONGO_DB_CONNECTION_STRING")
    val repository = MongoDataRepository(connectionString)
    val executor = Executor(timeProvider)
    val app = App(executor, repository, timeProvider)
    configureRouting(app.getClient())
    configureSerialization()
    launch { app.run() }
}