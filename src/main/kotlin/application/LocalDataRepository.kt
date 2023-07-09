package application

import model.WebsiteRecord

class LocalDataRepository : DataRepository {
    // Think about making it a map.
    private val websiteRecords = mutableListOf<WebsiteRecord>()
    override fun getWebsiteRecords(): List<WebsiteRecord> = websiteRecords
    override fun addWebsiteRecord(record: WebsiteRecord) = websiteRecords.add(record)
    override fun modifyWebsiteRecord(website: WebsiteRecord): Boolean {
        val success = websiteRecords.removeIf { website.url == it.url }
        if (success) {
            websiteRecords.add(website)
        }
        return success
    }

    override fun deleteWebsiteRecord(website: WebsiteRecord): Boolean = websiteRecords.remove(website)
}