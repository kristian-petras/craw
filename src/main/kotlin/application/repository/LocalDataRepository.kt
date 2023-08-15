package application.repository

import model.WebsiteRecord

class LocalDataRepository : DataRepository {
    private val records = mutableMapOf<Int, WebsiteRecord>()
    override suspend fun getAll(): List<WebsiteRecord> = records.values.toList()
    override suspend fun upsert(record: WebsiteRecord) : Boolean {
        if (!records.contains(record.id)) {
            return false
        }
        records[record.id] = record
        return true
    }

    override suspend fun delete(recordId: Int): Boolean {
        records.remove(recordId) ?: return false
        return true
    }

    override suspend fun get(recordId: Int): WebsiteRecord? = records[recordId]
}