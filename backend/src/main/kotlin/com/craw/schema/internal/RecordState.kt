package com.craw.schema.internal

import kotlin.time.Duration

data class RecordState(
    val recordId: String,
    val url: String,
    val regexp: String,
    val periodicity: Duration,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
    val executions: List<Execution>,
)
