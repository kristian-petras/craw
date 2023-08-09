package application

import model.Execution
import model.WebsiteRecord

interface DataRepository {
    fun getAll(): List<WebsiteRecord>
    fun add(record: WebsiteRecord)
    fun modify(record: WebsiteRecord) : Boolean
    fun delete(record: WebsiteRecord) : Boolean
    fun get(record: WebsiteRecord) : Execution?
    fun add(execution: Execution)
}