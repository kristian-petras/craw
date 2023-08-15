package ktor

import application.App
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.WebsiteRecordAdd
import model.WebsiteRecordDelete
import model.WebsiteRecordModify


fun Application.configureRouting(app: App.Client) {
    routing {
        route("/records") {
            get {
                val records = app.getAll()
                call.respond(HttpStatusCode.OK, records)
            }
        }
        route("/record") {
            put {
                val record = call.receive<WebsiteRecordModify>()
                val success = app.modify(record)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            post {
                val record = call.receive<WebsiteRecordAdd>()
                app.add(record)
                call.respond(HttpStatusCode.OK)
            }
            delete {
                val record = call.receive<WebsiteRecordDelete>()
                val success = app.delete(record)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
