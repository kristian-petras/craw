package com.craw.application

import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordUpdate
import com.craw.schema.rest.WebsiteRecord
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordDelete
import com.craw.schema.rest.WebsiteRecordUpdate
import com.craw.translator.RestTranslator
import kotlin.time.Duration

class RecordApplication(
    private val translator: RestTranslator,
    private val repository: Repository,
    private val executor: Executor,
) {
    fun getAll(): List<WebsiteRecord> {
        val records = repository.getRecords()
        return records.map { translator.translate(it) }
    }

    fun get(id: String): WebsiteRecord? {
        val record = repository.getRecord(id) ?: return null
        return translator.translate(record)
    }

    fun post(record: WebsiteRecordCreate): String {
        val newRecord = translator.translate(record)
        val recordState = repository.createRecord(newRecord)

        val schedule = newRecord.toSchedule(recordState.recordId)
        executor.schedule(schedule)

        return recordState.recordId
    }

    fun put(record: WebsiteRecordUpdate): Boolean {
        val updatedRecord = translator.translate(record)

        val schedule = updatedRecord.toSchedule()
        executor.remove(record.recordId)
        executor.schedule(schedule)

        return repository.updateRecord(updatedRecord)
    }

    fun delete(record: WebsiteRecordDelete): Boolean {
        executor.remove(record.recordId)
        return repository.deleteRecord(record.recordId)
    }

    private fun RecordCreate.toSchedule(id: String): ExecutionSchedule =
        ExecutionSchedule(
            recordId = id,
            url = this.baseUrl,
            regexp = this.regexp.toRegex(),
            periodicity = Duration.parse(this.periodicity),
        )

    private fun RecordUpdate.toSchedule(): ExecutionSchedule =
        ExecutionSchedule(
            recordId = this.recordId,
            url = this.baseUrl,
            regexp = this.regexp.toRegex(),
            periodicity = Duration.parse(this.periodicity),
        )
}
