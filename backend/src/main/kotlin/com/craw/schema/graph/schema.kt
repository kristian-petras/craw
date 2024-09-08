package com.craw.schema.graph

import kotlinx.datetime.Instant

data class Graph(
    val nodes: List<GraphRootNode>,
)

data class GraphRootNode(
    val executions: List<GraphExecution>,
    val nodes: List<GraphNode>,
)

enum class GraphExecutionType {
    SCHEDULED,
    RUNNING,
    COMPLETED
}

data class GraphExecution(
    val type: GraphExecutionType,
    val recordId: String,
    val executionId: String,
    val start: Instant,
    val end: Instant?,
)

enum class GraphNodeType {
    PENDING,
    RUNNING,
    COMPLETED,
    INVALID,
}

data class GraphNode(
    val type: GraphNodeType,
    val nodeId: String,
    val url: String,
    val title: String?,
    val start: Instant,
    val end: Instant?,
    val nodes: List<GraphNode>,
)