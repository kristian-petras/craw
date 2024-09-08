package com.craw.application

import com.craw.schema.internal.Execution

interface Crawler {
    suspend fun crawl(schedule: ExecutionSchedule): Execution
}