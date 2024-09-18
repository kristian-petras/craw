package com.craw.schema.graph

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Graph(
    val nodes: List<GraphRootNode>,
)

@Serializable
data class GraphRootNode(
    val record: GraphRecord,
    // ordered chronologically
    val executions: List<GraphExecution>,
    val node: GraphNode?,
)

@Serializable
enum class GraphExecutionType {
    PENDING,
    RUNNING,
    COMPLETED,
}

@Serializable
data class GraphRecord(
    val recordId: String,
    val url: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
)

@Serializable
data class GraphExecution(
    val type: GraphExecutionType,
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
    val url: String,
    val title: String?,
    val start: Instant?,
    val end: Instant?,
    val nodes: List<GraphNode>,
)
