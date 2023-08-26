package ktor

import application.App
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.WebsiteRecordAdd
import model.WebsiteRecordDelete
import model.WebsiteRecordModify


fun Application.configureRouting(app: App.Client) {
    routing {
        get("/records"){
            val records = app.getAll()
            call.respond(HttpStatusCode.OK, records)
        }
        post("/record") {
            val payload = call.receive<WebsiteRecordAdd>()
            val id = app.add(payload)
            call.respond(HttpStatusCode.OK, id)
        }
        put("/record") {
            val payload = call.receive<WebsiteRecordModify>()
            val success = app.modify(payload)
            val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status)
        }
        delete("/record") {
            val payload = call.receive<WebsiteRecordDelete>()
            val success = app.delete(payload)
            val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status)
        }
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
