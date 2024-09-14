package com.craw.application

import com.craw.schema.graph.Graph
import com.craw.schema.graph.GraphRootNode
import com.craw.schema.internal.RecordState
import com.craw.translator.SseTranslator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Publishes graph state to subscribers.
 * It is expected that all updates on records are propagated to this class.
 */
class GraphApplication(private val translator: SseTranslator, private val executor: Executor) {
    private val state = MutableStateFlow(Graph(emptyList()))

    private fun update(record: RecordState) {
        val oldGraph = state.value
        val node = translator.translate(record) ?: return
        state.value = oldGraph.replaceNode(node)
    }

    fun subscribe(): Flow<Graph> =
        channelFlow {
            launch {
                executor.subscribe().collect { update(it) }
            }
            state.asStateFlow().collect { send(it) }
        }

    private fun Graph.replaceNode(node: GraphRootNode): Graph {
        val newNodes = nodes.toMutableSet()
        newNodes.removeIf { it.record.recordId == node.record.recordId }
        newNodes.add(node)
        return Graph(newNodes.toList())
    }
}
