package com.craw.application

import kotlin.time.Duration

data class ExecutionSchedule(
    val recordId: String,
    val baseUrl: String,
    val regexp: Regex,
    val periodicity: Duration,
)