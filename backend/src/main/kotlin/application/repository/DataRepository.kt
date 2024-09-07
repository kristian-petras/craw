package application.repository

import model.WebsiteRecord

interface DataRepository {
    suspend fun getAll(): List<WebsiteRecord>

    suspend fun upsert(record: WebsiteRecord): Boolean

    suspend fun delete(recordId: Int): Boolean

    suspend fun get(recordId: Int): WebsiteRecord?
}
