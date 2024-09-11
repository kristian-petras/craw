package com.craw.utility

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.Duration.between
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun Duration.Companion.between(
    startInclusive: Instant,
    endExclusive: Instant,
): Duration = between(startInclusive.toJavaInstant(), endExclusive.toJavaInstant()).toKotlinDuration()
