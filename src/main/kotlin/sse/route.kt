package sse

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SseApp {
    private val events = MutableSharedFlow<Int>()

    suspend fun generateData() = coroutineScope {
        var n = 0
        while (true) {
            delay(1000)
            println("emitting $n")
            events.emit(n++)
        }
    }

    fun subscribeToEvents(): SharedFlow<Int> = events.asSharedFlow()
}

fun Application.sse(app: SseApp) {
    launch { app.generateData() }
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Craw - web crawler")
                    script {
                        type = "text/javascript"
                        src = "https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"
                    }
                    link {
                        rel = "stylesheet"
                        href = "https://cdn.datatables.net/1.13.6/css/jquery.dataTables.min.css"
                    }
                    script {
                        type = "text/javascript"
                        src = "https://code.jquery.com/jquery-3.6.0.min.js"
                    }
                    script {
                        type = "text/javascript"
                        src = "https://cdn.datatables.net/1.13.6/js/jquery.dataTables.min.js"
                    }
                }
                body {
                    div {
                        id = "root"
                        style = "width: 1000px; height: 1000px;"
                    }
                    ul {
                        id = "list"
                    }
                    table {
                        id = "example"
                        style = "width 100%"
                    }

                    script {
                        type = "text/javascript"
                        src = "/static/app.js"
                    }
                }
            }
        }
        get("/sse") {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.response.header("Connection", "keep-alive")
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                app.subscribeToEvents()
                    .onSubscription { println("subscribed!") }
                    .onStart { println("started") }
                    .onCompletion { println("completed $it") }
                    .onEmpty { println("empty") }
                    .onEach {
                        val node = Json.encodeToString(Node(it + 10, it.toString()))
                        println(node)
                        write("data: $node\n\n")
                        flush()
                    }
                    .flowOn(Dispatchers.IO)
                    .collect()
            }
        }
        staticResources("/static", "scripts")
    }

}

@Serializable
data class Node(val id: Int, val label: String)