package com.craw.schema.internal

data class RecordState(
    val recordId: String,
    val baseUrl: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
    val executions: List<Execution>,
)

