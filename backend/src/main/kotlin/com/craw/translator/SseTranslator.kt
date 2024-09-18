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

class SseTranslator {
    fun translate(record: RecordState): GraphRootNode =
        GraphRootNode(
            record = record.toGraphRecord(),
            executions = record.executions.map { it.toGraphExecution() },
            node = record.executions.lastOrNull()?.toGraphNode(),
        )

    private fun RecordState.toGraphRecord(): GraphRecord =
        GraphRecord(
            recordId = recordId,
            regexp = regexp,
            label = label,
            active = active,
            periodicity = periodicity.toIsoString(),
            tags = tags,
        )

    private fun Execution.toGraphExecution(): GraphExecution =
        when (this) {
            is Execution.Completed ->
                GraphExecution(
                    type = GraphExecutionType.COMPLETED,
                    executionId = executionId,
                    start = start,
                    end = end,
                )

            is Execution.Running ->
                GraphExecution(
                    type = GraphExecutionType.RUNNING,
                    executionId = executionId,
                    start = start,
                    end = null,
                )

            is Execution.Pending ->
                GraphExecution(
                    type = GraphExecutionType.PENDING,
                    executionId = executionId,
                    start = start,
                    end = null,
                )
        }

    private fun Execution.toGraphNode(): GraphNode = crawl.toGraphNode()

    private fun Crawl.toGraphNode(): GraphNode =
        when (this) {
            is Crawl.Completed ->
                GraphNode(
                    type = GraphNodeType.COMPLETED,
                    url = url.toString(),
                    title = title,
                    start = start,
                    end = end,
                    nodes = crawls.map { it.toGraphNode() },
                )

            is Crawl.Invalid ->
                GraphNode(
                    type = GraphNodeType.INVALID,
                    url = url.toString(),
                    title = null,
                    start = start,
                    end = end,
                    nodes = emptyList(),
                )

            is Crawl.Pending ->
                GraphNode(
                    type = GraphNodeType.PENDING,
                    url = url.toString(),
                    title = null,
                    start = null,
                    end = null,
                    nodes = emptyList(),
                )

            is Crawl.Running ->
                GraphNode(
                    type = GraphNodeType.RUNNING,
                    url = url.toString(),
                    title = null,
                    start = start,
                    end = null,
                    nodes = emptyList(),
                )
        }
}
