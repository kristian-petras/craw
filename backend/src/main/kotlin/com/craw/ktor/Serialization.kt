package com.craw.ktor

import kotlinx.serialization.json.Json

object Serialization {
    val json = Json {
        prettyPrint = true
    }
}