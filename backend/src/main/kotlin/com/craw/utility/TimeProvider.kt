package com.craw.utility

import kotlinx.datetime.Instant

fun interface TimeProvider {
    fun now(): Instant
}
