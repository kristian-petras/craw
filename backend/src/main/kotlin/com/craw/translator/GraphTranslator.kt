package com.craw.translator

import com.craw.schema.graph.GraphExecution
import com.craw.schema.graph.GraphExecutionType
import com.craw.schema.graph.GraphNode
import com.craw.schema.graph.GraphNodeType
import com.craw.schema.graph.GraphRecord
import com.craw.schema.graph.GraphRootNode
import com.craw.schema.internal.Crawl
import com.craw.schema.internal.Execution
import com.craw.schema.internal.RecordState

class GraphTranslator {
    fun translate(record: RecordState): GraphRootNode? {
        val execution = record.executions.lastOrNull() ?: return null
        return GraphRootNode(
            record = record.toGraphRecord(),
            execution = execution.toGraphExecution(),
            node = execution.toGraphNode()
        )
    }

    private fun RecordState.toGraphRecord(): GraphRecord = GraphRecord(
        recordId = recordId,
        regexp = regexp,
        label = label,
        active = active,
        periodicity = periodicity,
        tags = tags
    )

    private fun Execution.toGraphExecution(): GraphExecution = when (this) {
        is Execution.Completed -> GraphExecution(
            type = GraphExecutionType.COMPLETED,
            executionId = executionId,
            start = start,
            end = end
        )

        is Execution.Running -> GraphExecution(
            type = GraphExecutionType.RUNNING,
            executionId = executionId,
            start = start,
            end = null
        )

        is Execution.Scheduled -> GraphExecution(
            type = GraphExecutionType.SCHEDULED,
            executionId = executionId,
            start = start,
            end = null
        )
    }

    private fun Execution.toGraphNode(): GraphNode = when (this) {
        is Execution.Completed -> crawl.toGraphNode()
        is Execution.Running -> crawl.toGraphNode()

        is Execution.Scheduled -> GraphNode(
            type = GraphNodeType.PENDING,
            url = baseUrl,
            title = null,
            start = start,
            end = null,
            nodes = emptyList()
        )
    }

    private fun Crawl.toGraphNode(): GraphNode = when (this) {
        is Crawl.Completed -> GraphNode(
            type = GraphNodeType.COMPLETED,
            url = url,
            title = title,
            start = start,
            end = end,
            nodes = crawls.map { it.toGraphNode() }
        )

        is Crawl.Invalid -> GraphNode(
            type = GraphNodeType.INVALID,
            url = url,
            title = null,
            start = null,
            end = null,
            nodes = emptyList()
        )

        is Crawl.Pending -> GraphNode(
            type = GraphNodeType.PENDING,
            url = url,
            title = null,
            start = null,
            end = null,
            nodes = emptyList()
        )

        is Crawl.Running -> GraphNode(
            type = GraphNodeType.RUNNING,
            url = url,
            title = null,
            start = start,
            end = null,
            nodes = emptyList()
        )
    }
}