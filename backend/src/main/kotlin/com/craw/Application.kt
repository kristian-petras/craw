package com.craw

import com.craw.ktor.CrawlerServer
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

fun main() {
    val server = CrawlerServer(graphQLApplication = TODO(), graphApplication = TODO(), recordApplication = TODO())
    embeddedServer(
        factory = CIO,
        port = 8080,
        host = "0.0.0.0",
        module = { with(server) { module() } }
    ).start(wait = true)
}