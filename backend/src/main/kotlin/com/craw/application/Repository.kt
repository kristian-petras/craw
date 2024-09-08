package com.craw.application

import com.craw.schema.internal.Record
import com.craw.schema.internal.RecordCreate
import com.craw.schema.internal.RecordUpdate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class Repository(private val database: Database) {
    fun records(): List<Record> {
        TODO()
    }

    fun record(id: String): Record? {
        TODO()
    }

    /**
     * Deletes a record and all associated executions.
     */
    fun delete(recordId: String): Boolean {
        val x = transaction(database) {
            true
        }
        return x
    }

    fun create(record: RecordCreate): String {
        TODO()
    }

    fun update(record: RecordUpdate): Boolean {
        TODO()
    }
}