package domain.vis

import io.ktor.util.*
import kotlinx.serialization.Serializable
import model.WebsiteRecord

@Serializable
sealed interface VisModel {
    @Serializable
    data class Node(val id: Int, val label: String) : VisModel

    @Serializable
    data class Edge(val id: Int, val from: Int, val to: Int) : VisModel
}

fun WebsiteRecord.toGraph(): List<VisModel> {
    val execution = executions.lastOrNull() ?: return emptyList()

    val mainNode = VisModel.Node(this.id, this.url)
    val nodes = execution.crawledRecords.flatMap { record ->
        record.links.map {
            VisModel.Node(it.hashCode(), it)
        }
    }
    val edges = nodes.map { VisModel.Edge(id = Hash.combine(mainNode.id, it.id), from = mainNode.id, to = it.id) }

    return listOf(mainNode) + nodes + edges
}