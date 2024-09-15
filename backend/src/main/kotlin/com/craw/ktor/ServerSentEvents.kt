package com.craw.ktor

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.response.cacheControl
import io.ktor.server.response.header
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

object ServerSentEvents {
    fun Route.sse(
        endpoint: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        messages: () -> Flow<ServerSentEvent>,
    ) {
        get(endpoint) {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.response.header("Connection", "keep-alive")
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                messages()
                    .onEach {
                        if (it.event != null) {
                            write("event: ${it.event}\n\n")
                            call.application.log.info("event: ${it.event}")
                        }
                        write("data: ${it.data}\n\n")
                        call.application.log.info("data: ${it.data}")
                        flush()
                    }
                    .flowOn(dispatcher).collect()
            }
        }
    }

    data class ServerSentEvent(
        val event: String?,
        val data: String,
    )
}
