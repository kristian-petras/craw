package com.craw.ktor

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json

object UtilityModule {
    val json = Json {
        prettyPrint = true
    }

    fun Application.install() {
        install(ContentNegotiation) {
            json(json)
        }
        install(CORS) {
            anyHost()
            allowHeader(io.ktor.http.HttpHeaders.ContentType)
        }
    }
}