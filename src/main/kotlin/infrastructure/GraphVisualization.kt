package infrastructure

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.*
import sse.SseApp
import java.io.Writer

internal fun Application.sse(app: SseApp) {
    routing {
        get("/graph") { call.respondHtml { graph() } }
        get("/graph-events") {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.response.header("Connection", "keep-alive")
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                connectToSse(app)
            }
        }
        staticResources("/static", "scripts")
    }
}

private suspend fun Writer.connectToSse(app: SseApp) {
    app.subscribeToEvents()
        .onEach {
            write(it)
            flush()
        }
        .flowOn(Dispatchers.IO)
        .collect()
}

private fun HTML.graph() {
    head {
        title("Craw - Graph")
        script {
            type = "text/javascript"
            src = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"
        }
    }
    body {
        div {
            id = "root"
            style = "width: 1000px; height: 1000px;"
        }
        script {
            type = "text/javascript"
            src = "/static/graph.js"
        }
    }
}