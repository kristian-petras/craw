package sse

import domain.vis.VisModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SseApp {
    private val events = MutableSharedFlow<String>()

    suspend fun sendUpdate(update: List<VisModel>) {
        println("sending update $update")
        update.forEach {
            val payload =
                when (it) {
                    is VisModel.Edge -> Json.encodeToString(it).also { events.emit("event: edge\n") }
                    is VisModel.Node -> Json.encodeToString(it).also { events.emit("event: node\n") }
                }
            events.emit("data: $payload\n\n")
        }
    }

    fun subscribeToEvents(): SharedFlow<String> = events.asSharedFlow()
}
