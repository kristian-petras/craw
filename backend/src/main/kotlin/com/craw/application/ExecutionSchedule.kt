package com.craw.application

import kotlin.time.Duration

data class ExecutionSchedule(
    val recordId: String,
    val url: String,
    val regexp: Regex,
    val periodicity: Duration,
)