import com.craw.schema.Node
import com.craw.schema.Query
import com.craw.schema.WebPage
import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class PudinkQuery : Query {
    override fun websites(): List<WebPage> {
        return listOf(
            WebPage(
                identifier = ID("1"),
                label = "Google",
                url = "https://www.google.com",
                regexp = ".*",
                tags = listOf("search", "engine"),
                active = true
            )
        )
    }

    override fun nodes(webPages: List<ID>): List<Node> {
        TODO("Not yet implemented")
    }
}

fun Application.configureRouting() {
    install(GraphQL) {
        schema {
            packages = listOf("com.craw.schema")
            queries = listOf(
                PudinkQuery()
            )
        }
    }
    routing {
        graphQLGetRoute()
        graphQLPostRoute()
        //graphQLSubscriptionsRoute()
        graphiQLRoute()
        graphQLSDLRoute()

        get("/") {
            call.respondText("Hello World!")
        }

        swaggerUI("/swagger", swaggerFile = "openapi/documentation.yaml")
    }
}

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
