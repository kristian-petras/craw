package com.craw.schema.graph

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Graph(
    val nodes: List<GraphRootNode>,
)

@Serializable
data class GraphRootNode(
    val execution: GraphExecution,
    val node: GraphNode,
)

@Serializable
enum class GraphExecutionType {
    SCHEDULED,
    RUNNING,
    COMPLETED
}

@Serializable
data class GraphExecution(
    val type: GraphExecutionType,
    val recordId: String,
    val executionId: String,
    val start: Instant,
    val end: Instant?,
)

@Serializable
enum class GraphNodeType {
    PENDING,
    RUNNING,
    COMPLETED,
    INVALID,
}

@Serializable
data class GraphNode(
    val type: GraphNodeType,
    val nodeId: String,
    val url: String,
    val title: String?,
    val start: Instant,
    val end: Instant?,
    val nodes: List<GraphNode>,
)