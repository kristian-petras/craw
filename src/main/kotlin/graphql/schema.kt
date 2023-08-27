package graphql

import com.apurebase.kgraphql.GraphQL
import io.ktor.server.application.*

data class WebPage(
    val identifier: String,
    val label: String,
    val url: String,
    val regexp: String,
    val tags: List<String>,
    val active: Boolean
)

data class Node(
    val title: String?,
    val url: String,
    val links: List<Node>,
    val owner: WebPage
)

fun Application.graphQL() {
    install(GraphQL) {
        playground = true
        schema {
            type<WebPage>()
            type<Node>()

            query("websites") {
                resolver { -> "world" }
            }

            query("nodes") {
                resolver { webPages: List<String> -> "nodes" }
            }
        }
    }
}
