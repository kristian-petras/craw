package application

import model.Execution
import model.WebsiteRecord

class LocalDataRepository : DataRepository {
    private val records = mutableMapOf<Long, WebsiteRecord>()
    private val executions = mutableMapOf<Long, Execution>()
    override fun getAll(): List<WebsiteRecord> = records.values.toList()
    override fun add(record: WebsiteRecord) {
        records[record.id] = record
    }

    override fun add(execution: Execution) {
        executions[execution.recordId] = execution
    }

    override fun modify(record: WebsiteRecord): Boolean {
        if (!records.contains(record.id)) {
            return false
        }
        records[record.id] = record
        return true
    }

    override fun delete(record: WebsiteRecord): Boolean {
        executions.remove(record.id)
        records.remove(record.id) ?: return false
        return true
    }

    override fun getExecution(recordId: Long): Execution? = executions[recordId]
    override fun getRecord(recordId: Long): WebsiteRecord? = records[recordId]
}