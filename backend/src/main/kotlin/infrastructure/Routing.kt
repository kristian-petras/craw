package infrastructure

import application.App
import domain.graphql.QueryResolver
import io.ktor.server.application.*
import sse.SseApp

fun Application.configureRouting(sseApp: SseApp, client: App.Client, queryResolver: QueryResolver) {
    documentation()
    graphQL(queryResolver)
    rest(client)
    sse(sseApp)
    table()
}