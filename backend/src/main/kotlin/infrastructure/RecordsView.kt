package infrastructure

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

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
        table {
            id = "example"
            style = "width 100%"
        }
        script {
            type = "text/javascript"
            src = "/static/table.js"
        }
    }
}