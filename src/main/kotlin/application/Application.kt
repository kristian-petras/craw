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
    deleteRecord(repository)
)

private fun getRecords(repository: DataRepository) = "/records" bind Method.GET to {
    val records = repository.getWebsiteRecords()
    Response(Status.OK)
        .with(Body.json().toLens() of records.asJsonObject())
}

private fun addRecord(repository: DataRepository) = "/record" bind Method.POST to {
    handleRequest(it) { record ->
        repository.addWebsiteRecord(record)
    }
}

private fun modifyRecord(repository: DataRepository) = "/record" bind Method.PUT to {
    handleRequest(it) { record ->
        repository.modifyWebsiteRecord(record)
    }
}

private fun deleteRecord(repository: DataRepository) = "/record" bind Method.DELETE to {
    handleRequest(it) { record ->
        repository.deleteWebsiteRecord(record)
    }
}

private fun handleRequest(request: Request, block: (WebsiteRecord) -> Boolean) : Response {
    val record = WebsiteRecord.lens(request).orThrow()
    val success = block(record)
    return if (success) {
        Response(Status.ACCEPTED)
    } else {
        Response(Status.BAD_REQUEST)
    }
}