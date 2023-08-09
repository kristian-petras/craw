package ktor

import application.App
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.WebsiteRecord


fun Application.configureRouting(app: App.Client) {
    routing {
        route("/records") {
            get {
                call.respond(app.getAll())
            }
        }
        route("/record") {
            put {
                val record = call.receive<WebsiteRecord>()
                val success = app.modify(record)
                if (success) {
                    call.respondText { "Record modified successfully." }
                } else {
                    call.respondText(status = HttpStatusCode.BadRequest) { "Whoops!" }
                }
            }
            post {
                val record = call.receive<WebsiteRecord>()
                app.add(record)
                call.respondText { "Record added successfully" }
            }
            delete {
                val record = call.receive<WebsiteRecord>()
                val success = app.delete(record)
                if (success) {
                    call.respondText { "Record deleted successfully." }
                } else {
                    call.respondText(status = HttpStatusCode.BadRequest) { "Whoops!" }
                }
            }
        }
    }
}
