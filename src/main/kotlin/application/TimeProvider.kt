package application

import java.time.Instant

fun interface TimeProvider {
    fun now(): Instant
}