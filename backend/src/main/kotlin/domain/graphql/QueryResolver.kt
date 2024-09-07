package domain.graphql

interface QueryResolver {
    suspend fun websites(): List<WebPage>

    suspend fun nodes(webPages: List<Identifier>): List<Node>
}
