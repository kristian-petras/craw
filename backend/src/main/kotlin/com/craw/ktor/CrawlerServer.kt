package com.craw.ktor

import com.craw.application.GraphApplication
import com.craw.application.GraphQLApplication
import com.craw.application.RecordApplication
import com.craw.ktor.ServerSentEvents.ServerSentEvent
import com.craw.ktor.ServerSentEvents.sse
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordDelete
import com.craw.schema.rest.WebsiteRecordUpdate
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun Application.module(
    graphQLApplication: GraphQLApplication,
    graphApplication: GraphApplication,
    recordApplication: RecordApplication,
) {
    install(GraphQL) {
        schema {
            packages = listOf("com.craw.schema.graphql")
            queries = listOf(graphQLApplication)
        }
    }

    routing {
        install(ContentNegotiation) {
            json(json)
        }
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
        }
        restRoutes(recordApplication)
        sseRoutes(graphApplication)
        graphQLRoutes()
        swaggerRoutes()
    }
}

private fun Route.restRoutes(app: RecordApplication) {
    route("/records") {
        get {
            val records = app.getAll()
            call.respond(HttpStatusCode.OK, records)
        }
    }
    route("/record") {
        get {
            val id = call.parameters["id"]
            val record = id?.let { app.get(it) }
            if (record != null) {
                call.respond(HttpStatusCode.OK, record)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post {
            val payload = call.receive<WebsiteRecordCreate>()
            val id = app.post(payload)
            call.respond(HttpStatusCode.OK, id)
        }
        put {
            val payload = call.receive<WebsiteRecordUpdate>()
            val success = app.put(payload)
            val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status)
        }
        delete {
            val payload = call.receive<WebsiteRecordDelete>()
            val success = app.delete(payload)
            val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status)
        }
    }
}

private fun Route.sseRoutes(app: GraphApplication) {
    sse("graph") {
        app.subscribe()
            .map { json.encodeToString(it) }
            .map { ServerSentEvent(null, it) }
    }
}

private fun Route.graphQLRoutes() {
    graphQLGetRoute()
    graphQLPostRoute()
    // graphQLSubscriptionsRoute()
    graphiQLRoute()
    graphQLSDLRoute()
}

private fun Route.swaggerRoutes() {
    swaggerUI("/swagger", swaggerFile = "openapi/documentation.yaml")
}

private val json =
    Json {
        prettyPrint = true
    }
