package model

import java.time.Instant

data class Event<T>(val timestamp: Instant, val payload: T)