package com.craw.application

import com.craw.schema.internal.Execution
import kotlinx.coroutines.flow.Flow

interface Executor {
    fun schedule(schedule: ExecutionSchedule) {
        TODO("Not yet implemented")
    }

    fun remove(recordId: String) {
        TODO("Not yet implemented")
    }

    fun subscribe(): Flow<Execution>
}