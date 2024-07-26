package infrastructure

import com.apurebase.kgraphql.GraphQL
import domain.graphql.Identifier
import domain.graphql.Node
import domain.graphql.QueryResolver
import domain.graphql.WebPage
import io.ktor.server.application.*

internal fun Application.graphQL(queryResolver: QueryResolver) {
    install(GraphQL) {
        playground = true
        schema {
            type<WebPage>()
            type<Node>()

            query("websites") {
                resolver { -> queryResolver.websites() }
            }

            query("nodes") {
                resolver { webPages: List<Identifier> -> queryResolver.nodes(webPages) }
            }
        }
    }
}