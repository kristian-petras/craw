package application

import model.WebsiteRecord

class LocalDataRepository : DataRepository {
    private val websiteRecords = mutableListOf<WebsiteRecord>()
    override fun getWebsiteRecords(): List<WebsiteRecord> = websiteRecords
    override fun addWebsiteRecord(record: WebsiteRecord) = websiteRecords.add(record)
}