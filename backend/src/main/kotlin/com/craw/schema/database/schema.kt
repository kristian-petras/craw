package com.craw.schema.database

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID


object RecordsTable : UUIDTable() {
    val name = varchar("name", 255)
    val url = varchar("url", 255)
    val regexp = varchar("regexp", 255)
    val periodicity = varchar("periodicity", 255)
    val label = varchar("label", 255)
    val tags = array<String>("tags")
    val active = bool("active")
}

class RecordEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RecordEntity>(RecordsTable)

    var name by RecordsTable.name
    var url by RecordsTable.url
    var regexp by RecordsTable.regexp
    var periodicity by RecordsTable.periodicity
    var label by RecordsTable.label
    var active by RecordsTable.active
    var tags by RecordsTable.tags
    val executions by ExecutionEntity referrersOn ExecutionsTable.record
}

object CrawlRelationsTable : Table() {
    val parent = reference("parent_id", CrawlsTable)
    val child = reference("child_id", CrawlsTable)
}

object CrawlsTable : UUIDTable() {
    val url = varchar("url", 255)
    val title = varchar("title", 255).nullable()
    val start = timestamp("start")
    val end = timestamp("end").nullable()
    val execution = reference("execution", ExecutionsTable)
}

class CrawlEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CrawlEntity>(CrawlsTable)

    var url by CrawlsTable.url
    var title by CrawlsTable.title
    var start by CrawlsTable.start
    var end by CrawlsTable.end
    var execution by ExecutionEntity referencedOn CrawlsTable.execution
    var parents by CrawlEntity.via(CrawlRelationsTable.child, CrawlRelationsTable.parent)
    var children by CrawlEntity.via(CrawlRelationsTable.parent, CrawlRelationsTable.child)
}

object ExecutionsTable : UUIDTable() {
    val url = varchar("url", 255)
    val regexp = varchar("regexp", 255)
    val start = timestamp("start")
    val end = timestamp("end").nullable()
    val record = reference("record", RecordsTable)
}

class ExecutionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExecutionEntity>(ExecutionsTable)

    var url by ExecutionsTable.url
    var regexp by ExecutionsTable.regexp
    var start by ExecutionsTable.start
    var end by ExecutionsTable.end
    var record by RecordEntity referencedOn ExecutionsTable.record
    val crawls by CrawlEntity referrersOn CrawlsTable.execution
}
