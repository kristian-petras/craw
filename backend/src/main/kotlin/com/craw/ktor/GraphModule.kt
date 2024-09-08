package com.craw.ktor

import com.craw.GraphApplication
import com.craw.ktor.Serialization.json
import com.craw.ktor.ServerSentEvents.ServerSentEvent
import com.craw.ktor.ServerSentEvents.sse
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString

object GraphModule {
    fun Route.routes(app: GraphApplication) {
        sse("graph") {
            app.subscribe()
                .map { json.encodeToString(it) }
                .map { ServerSentEvent(null, it) }
        }
    }
}

