package application

import dev.forkhandles.result4k.orThrow
import kotlinx.coroutines.runBlocking
import model.WebsiteRecord
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.routing.bind
import org.http4k.routing.routes

fun server(app: App.Client) = routes(
    getRecords(app),
    addRecord(app),
    modifyRecord(app),
    deleteRecord(app)
)

private fun getRecords(app: App.Client) = "/records" bind Method.GET to {
    val records = runBlocking {
        app.getAll()
    }

    Response(Status.OK)
        .with(Body.json().toLens() of records.asJsonObject())
}

private fun addRecord(app: App.Client) = "/record" bind Method.POST to {
    handleRequest(it) { record ->
        app.add(record)
        true
    }
}

private fun modifyRecord(app: App.Client) = "/record" bind Method.PUT to {
    handleRequest(it) { record ->
        app.modify(record)
    }
}

private fun deleteRecord(app: App.Client) = "/record" bind Method.DELETE to {
    handleRequest(it) { record ->
        app.delete(record)
    }
}

private fun handleRequest(request: Request, block: suspend (WebsiteRecord) -> Boolean): Response = runBlocking {
    val record = WebsiteRecord.lens(request).orThrow()
    val success = block(record)
    if (success) {
        Response(Status.ACCEPTED)
    } else {
        Response(Status.BAD_REQUEST)
    }
}
