package ktor

import application.App
import application.Executor
import application.LocalDataRepository
import application.TimeProvider
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import java.time.Instant

fun Application.module() {
    val timeProvider = TimeProvider { Instant.now() }
    val repository = LocalDataRepository()
    val executor = Executor(timeProvider)
    val app = App(executor, repository)
    launch { app.run() }
    configureRouting(app.getClient())
    configureSerialization()
}