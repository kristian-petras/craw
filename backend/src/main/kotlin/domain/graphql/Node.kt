package domain.graphql

data class Node(
    val title: String?,
    val url: String,
    val crawlTime: String?,
    val links: List<Node>,
    val owner: WebPage,
)
