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
            execution = record.executions.map { it.toGraphExecution(record.label) },
            node = record.executions.lastOrNull()?.toGraphNode(),
        )

    private fun RecordState.toGraphRecord(): GraphRecord =
        GraphRecord(
            recordId = recordId,
            url = url,
            regexp = regexp,
            label = label,
            active = active,
            periodicity = periodicity.toIsoString(),
            tags = tags,
        )

    private fun Execution.toGraphExecution(label: String): GraphExecution =
        when (this) {
            is Execution.Completed ->
                GraphExecution(
                    label = label,
                    type = GraphExecutionType.COMPLETED,
                    executionId = executionId,
                    start = start,
                    end = end,
                    crawledCount = crawl.toGraphNode().toCrawlCount(),
                )

            is Execution.Running ->
                GraphExecution(
                    label = label,
                    type = GraphExecutionType.RUNNING,
                    executionId = executionId,
                    start = start,
                    end = null,
                    crawledCount = crawl.toGraphNode().toCrawlCount(),
                )

            is Execution.Pending ->
                GraphExecution(
                    label = label,
                    type = GraphExecutionType.PENDING,
                    executionId = executionId,
                    start = start,
                    end = null,
                    crawledCount = crawl.toGraphNode().toCrawlCount(),
                )
        }

    private fun GraphNode.toCrawlCount(): Int = nodes.count() + nodes.sumOf { it.toCrawlCount() }

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
