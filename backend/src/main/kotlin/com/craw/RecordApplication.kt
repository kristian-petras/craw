package com.craw

import com.craw.schema.rest.WebsiteRecord
import com.craw.schema.rest.WebsiteRecordCreate
import com.craw.schema.rest.WebsiteRecordDelete
import com.craw.schema.rest.WebsiteRecordUpdate

interface RecordApplication {
    fun getAll(): List<WebsiteRecord>
    fun get(id: String): WebsiteRecord?
    fun post(record: WebsiteRecordCreate): String
    fun put(record: WebsiteRecordUpdate): Boolean
    fun delete(record: WebsiteRecordDelete): Boolean
}