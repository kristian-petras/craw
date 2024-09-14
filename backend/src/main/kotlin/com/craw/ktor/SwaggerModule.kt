package com.craw.ktor

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

object SwaggerModule {
    fun Route.routes() {
        swaggerUI("/swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
