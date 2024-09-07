package infrastructure

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.cacheControl
import io.ktor.server.response.header
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
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
