package utility

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun Duration.Companion.between(
    startInclusive: Instant,
    endExclusive: Instant,
): Duration = java.time.Duration.between(startInclusive, endExclusive).toKotlinDuration()
