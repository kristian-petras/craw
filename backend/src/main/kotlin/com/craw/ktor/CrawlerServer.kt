package com.craw.ktor

import com.craw.GraphApplication
import com.craw.GraphQLApplication
import com.craw.RecordApplication
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

class CrawlerServer(
    private val graphQLApplication: GraphQLApplication,
    private val graphApplication: GraphApplication,
    private val recordApplication: RecordApplication,
) {
    fun Application.module() {
        routing {
            with(GraphModule) {
                routes(graphApplication)
            }
            with(GraphQLModule) {
                routes()
            }
            with(RecordModule) {
                routes(recordApplication)
            }
            with(SwaggerModule) {
                routes()
            }
        }

        with(SerializationModule) {
            install()
        }
        with(GraphQLModule) {
            install(graphQLApplication)
        }
    }
}