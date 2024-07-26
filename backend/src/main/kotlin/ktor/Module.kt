package ktor

import application.App
import application.Executor
import application.repository.LocalDataRepository
import application.repository.MongoDataRepository
import domain.graphql.Identifier
import domain.graphql.Node
import domain.graphql.QueryResolver
import domain.graphql.WebPage
import infrastructure.configureRouting
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import model.WebsiteRecord
import sse.SseApp
import utility.TimeProvider
import java.time.Instant

fun Application.module() {
    val timeProvider = TimeProvider { Instant.now() }
    val env = environment.config.propertyOrNull("ktor.environment")?.getString()
    val repository = when (env) {
        "dev" -> MongoDataRepository(System.getenv("MONGO_DB_CONNECTION_STRING"))
        "test" -> LocalDataRepository()
        else -> LocalDataRepository()
    }

    val executor = Executor(timeProvider)
    val sseApp = SseApp()
    val app = App(executor, repository, timeProvider, sseApp)

    val client = app.getClient()
    configureRouting(
        sseApp,
        client,
        DelegatingQueryResolver(client)
    )
    launch { app.run() }
}

class DelegatingQueryResolver(private val app: App.Client) : QueryResolver {
    override suspend fun websites(): List<WebPage> = app.getAll().map { it.toWebPage() }

    override suspend fun nodes(webPages: List<Identifier>): List<Node> =
        app.getAll()
            .filter { it.toIdentifier() in webPages }
            .map { Node(null, it.url, null, emptyList(), it.toWebPage()) }

    private fun WebsiteRecord.toWebPage(): WebPage = WebPage(
        identifier = id.toString(),
        label = label,
        url = url,
        regexp = boundaryRegExp,
        tags = tags,
        active = active
    )

    private fun WebsiteRecord.toIdentifier(): Identifier = id.toString()
}