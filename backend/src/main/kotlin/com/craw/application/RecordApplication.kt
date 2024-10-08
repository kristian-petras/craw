package com.craw.application

import com.craw.schema.rest.WebsiteRecord
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordUpdate
import com.craw.translator.RestTranslator

class RecordApplication(
    private val translator: RestTranslator,
    private val repository: Repository,
    private val executor: Executor,
) {
    fun getAll(): List<WebsiteRecord> {
        val records = repository.getRecords()
        return records.map { translator.translate(it) }
    }

    fun get(recordId: String): WebsiteRecord? {
        val record = repository.getRecord(recordId) ?: return null
        return translator.translate(record)
    }

    fun post(record: WebsiteRecordCreate): String? {
        val newRecord = translator.translate(record)
        val recordState = repository.createRecord(newRecord) ?: return null

        executor.schedule(recordState.recordId)

        return recordState.recordId
    }

    fun put(record: WebsiteRecordUpdate): Boolean {
        val oldRecord = repository.getRecord(record.recordId) ?: return false
        val updatedRecord = translator.translate(oldRecord, record)
        val recordState = repository.updateRecord(updatedRecord) ?: return false

        executor.schedule(recordState.recordId)

        return true
    }

    fun delete(recordId: String): Boolean {
        executor.remove(recordId)
        return repository.deleteRecord(recordId)
    }
}
