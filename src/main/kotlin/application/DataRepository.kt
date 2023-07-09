package application

import model.WebsiteRecord

interface DataRepository {
    fun getWebsiteRecords(): List<WebsiteRecord>
    fun addWebsiteRecord(record: WebsiteRecord) : Boolean
    fun modifyWebsiteRecord(website: WebsiteRecord) : Boolean
}