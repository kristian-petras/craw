package com.craw.schema.internal

data class Record(
    val recordId: String,
    val baseUrl: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
    val executions: List<Execution>,
)

data class RecordCreate(
    val baseUrl: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
)

data class RecordDelete(
    val recordId: String,
)

data class RecordUpdate(
    val recordId: String,
    val baseUrl: String,
    val regexp: String,
    val periodicity: String,
    val label: String,
    val active: Boolean,
    val tags: List<String>,
)