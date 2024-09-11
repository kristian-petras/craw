package com.craw.ktor

import com.craw.application.Executor
import com.craw.application.GraphApplication
import com.craw.application.GraphQLApplication
import com.craw.application.RecordApplication
import com.craw.application.Repository
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.launch

class CrawlerServer(
    private val graphQLApplication: GraphQLApplication,
    private val graphApplication: GraphApplication,
    private val recordApplication: RecordApplication,
    private val executor: Executor,
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

        with(UtilityModule) {
            install()
        }
        with(GraphQLModule) {
            install(graphQLApplication)
        }
    }
}