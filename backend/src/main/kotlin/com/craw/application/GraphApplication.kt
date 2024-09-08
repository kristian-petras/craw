package com.craw.application

import com.craw.schema.graph.Graph
import com.craw.schema.graph.GraphRootNode
import com.craw.schema.internal.Record
import com.craw.translator.GraphTranslator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Publishes graph state to subscribers.
 * It is expected that all updates on records are propagated to this class.
 */
class GraphApplication(private val translator: GraphTranslator) {
    private val state = MutableStateFlow(Graph(emptyList()))

    fun update(record: Record) {
        val oldGraph = state.value
        val node = translator.translate(record) ?: return
        state.value = oldGraph.replaceNode(node)
    }

    fun subscribe(): Flow<Graph> = state.asStateFlow()

    private fun Graph.replaceNode(node: GraphRootNode): Graph {
        val newNodes = nodes.toMutableSet()
        newNodes.removeIf { it.record.recordId == node.record.recordId }
        newNodes.add(node)
        return Graph(newNodes.toList())
    }
}