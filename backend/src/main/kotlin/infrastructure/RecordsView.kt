package infrastructure

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.title

internal fun Application.table() {
    routing {
        get("/table") {
            call.respondHtml { table() }
        }
    }
}

private fun HTML.table() {
    head {
        title("Craw - Records")
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
        script {
            type = "text/javascript"
            src = "/static/table.js"
        }
    }
}
