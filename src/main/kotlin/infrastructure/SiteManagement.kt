package infrastructure

import application.App
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.WebsiteRecordAdd
import model.WebsiteRecordDelete
import model.WebsiteRecordModify

internal fun Application.rest(app: App.Client) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/records") {
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
    }
}
