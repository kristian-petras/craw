package com.craw.application

import com.craw.schema.graph.Graph
import com.craw.schema.graph.GraphRootNode
import com.craw.schema.internal.RecordState
import com.craw.translator.SseTranslator
import com.craw.utility.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow

/**
 * Publishes graph state to subscribers.
 * It is expected that all updates on records are propagated to this class.
 */
class GraphApplication(private val translator: SseTranslator) {
    private val state = MutableStateFlow(Graph(emptyList()))

    fun update(record: RecordState) {
        val oldGraph = state.value
        val node = translator.translate(record)
        state.value = oldGraph.replaceNode(node)
        logger.info("Updated graph to new state ${state.value}")
    }

    fun subscribe(): Flow<Graph> =
        channelFlow {
            state.asStateFlow().collect { send(it) }
        }

    private fun Graph.replaceNode(node: GraphRootNode): Graph {
        val newNodes = nodes.toMutableSet()
        newNodes.removeIf { it.record.recordId == node.record.recordId }
        newNodes.add(node)
        return Graph(newNodes.toList())
    }

    fun remove(recordId: String) {
        val oldGraph = state.value
        state.value = oldGraph.copy(nodes = oldGraph.nodes.filterNot { it.record.recordId == recordId })
        logger.info("Removed node $recordId from graph")
    }

    companion object {
        private val logger = logger<GraphApplication>()
    }
}
