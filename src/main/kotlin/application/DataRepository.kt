package application

import model.WebsiteRecord

interface DataRepository {
    fun getWebsiteRecords(): List<WebsiteRecord>
}