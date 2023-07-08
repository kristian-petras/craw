package application

import org.http4k.core.*
import org.http4k.format.KotlinxSerialization.asJsonObject
import org.http4k.format.KotlinxSerialization.json
import org.http4k.routing.bind
import org.http4k.routing.routes

fun app(repository: DataRepository) = routes(
    records(repository),
)

private fun records(repository: DataRepository) = "/records" bind Method.GET to { _: Request ->
    val records = repository.getWebsiteRecords()
    Response(Status.OK)
        .with(Body.json().toLens() of records.asJsonObject())
}
