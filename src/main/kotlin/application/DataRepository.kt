package application

import model.Execution
import model.WebsiteRecord

interface DataRepository {
    fun getAll(): List<WebsiteRecord>
    fun add(record: WebsiteRecord)
    fun modify(record: WebsiteRecord) : Boolean
    fun delete(record: WebsiteRecord) : Boolean
    fun getExecution(recordId: Long) : Execution?
    fun getRecord(recordId: Long) : WebsiteRecord?
    fun add(execution: Execution)
}