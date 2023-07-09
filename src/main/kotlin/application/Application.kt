package application

import dev.forkhandles.result4k.orThrow
import model.WebsiteRecord
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.routing.bind
import org.http4k.routing.routes

fun app(repository: DataRepository) = routes(
    getRecords(repository),
    addRecord(repository),
    modifyRecord(repository),
    hello()
)

private fun getRecords(repository: DataRepository) = "/records" bind Method.GET to {
    val records = repository.getWebsiteRecords()
    Response(Status.OK)
        .with(Body.json().toLens() of records.asJsonObject())
}

private fun addRecord(repository: DataRepository) = "/record" bind Method.POST to {
    val record = WebsiteRecord.lens(it).orThrow()
    val success = repository.addWebsiteRecord(record)
    if (success) {
        Response(Status.ACCEPTED)
    } else {
        Response(Status.BAD_REQUEST)
    }
}

private fun modifyRecord(repository: DataRepository) = "/record" bind Method.PUT to {
    val record = WebsiteRecord.lens(it).orThrow()
    val success = repository.modifyWebsiteRecord(record)
    if (success) {
        Response(Status.ACCEPTED)
    } else {
        Response(Status.BAD_REQUEST)
    }
}

private fun hello() = "/" bind Method.GET to { Response(Status.OK).body("Hello World!") }