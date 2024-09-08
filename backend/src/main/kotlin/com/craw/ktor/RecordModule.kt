package com.craw.ktor

import com.craw.RecordApplication
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordDelete
import com.craw.schema.rest.WebsiteRecordUpdate
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

object RecordModule {
    fun Route.routes(app: RecordApplication) {
        route("/records") {
            get {
                val records = app.getAll()
                call.respond(HttpStatusCode.OK, records)
            }
        }
        route("/record") {
            get {
                val id = call.parameters["id"]
                val record = id?.let { app.get(it) }
                if (record != null) {
                    call.respond(HttpStatusCode.OK, record)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post {
                val payload = call.receive<WebsiteRecordCreate>()
                val id = app.post(payload)
                call.respond(HttpStatusCode.OK, id)
            }
            put {
                val payload = call.receive<WebsiteRecordUpdate>()
                val success = app.put(payload)
                val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status)
            }
            delete {
                val payload = call.receive<WebsiteRecordDelete>()
                val success = app.delete(payload)
                val status = if (success) HttpStatusCode.OK else HttpStatusCode.BadRequest
                call.respond(status)
            }
        }
    }
}

