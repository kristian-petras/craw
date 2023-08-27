package sse

import domain.vis.Node
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SseApp {
    private val events = MutableSharedFlow<String>()
    suspend fun sendUpdate(node: Node) {
        val payload = Json.encodeToString(node)
        events.emit("data: $payload\n\n")
    }

    fun subscribeToEvents(): SharedFlow<String> = events.asSharedFlow()
}
