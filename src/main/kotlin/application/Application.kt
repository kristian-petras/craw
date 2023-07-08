package application

import dev.forkhandles.result4k.orThrow
import model.WebsiteRecord
import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.routing.bind
import org.http4k.routing.routes

fun app(repository: DataRepository) = routes(
    records(repository),
    record(repository),
    hello()
)

private fun records(repository: DataRepository) = "/records" bind Method.GET to {
    val records = repository.getWebsiteRecords()
    Response(Status.OK)
        .with(Body.json().toLens() of records.asJsonObject())
}

private fun record(repository: DataRepository) = "/record" bind Method.POST to {
    val record = WebsiteRecord.lens(it).orThrow()
    repository.addWebsiteRecord(record)
    Response(Status.ACCEPTED)
}

private fun hello() = "/" bind Method.GET to { Response(Status.OK).body("Hello World!") }