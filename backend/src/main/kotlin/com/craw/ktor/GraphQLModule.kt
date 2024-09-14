package com.craw.ktor

import com.craw.application.GraphQLApplication
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.Route

object GraphQLModule {
    fun Route.routes() {
        graphQLGetRoute()
        graphQLPostRoute()
        // graphQLSubscriptionsRoute()
        graphiQLRoute()
        graphQLSDLRoute()
    }

    fun Application.install(app: GraphQLApplication) {
        install(GraphQL) {
            schema {
                packages = listOf("com.craw.schema.graphql")
                queries = listOf(app)
            }
        }
    }
}
