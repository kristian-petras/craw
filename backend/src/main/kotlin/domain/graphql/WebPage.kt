package domain.graphql

typealias Identifier = String

data class WebPage(
    val identifier: Identifier,
    val label: String,
    val url: String,
    val regexp: String,
    val tags: List<String>,
    val active: Boolean,
)
