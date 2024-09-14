package com.craw.schema.database

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID


object RecordsTable : UUIDTable() {
    val url = varchar("url", 255)
    val regexp = varchar("regexp", 255)
    val periodicity = varchar("periodicity", 255)
    val label = varchar("label", 255)
    val tags = array<String>("tags")
    val active = bool("active")
}

class RecordEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RecordEntity>(RecordsTable)

    var url by RecordsTable.url
    var regexp by RecordsTable.regexp
    var periodicity by RecordsTable.periodicity
    var label by RecordsTable.label
    var active by RecordsTable.active
    var tags by RecordsTable.tags
    val executions by ExecutionEntity referrersOn ExecutionsTable.record
}

object CrawlRelationsTable : Table() {
    val parent = reference("parent_id", CrawlsTable, onDelete = ReferenceOption.CASCADE)
    val child = reference("child_id", CrawlsTable, onDelete = ReferenceOption.CASCADE)
}

enum class CrawlType {
    PENDING,
    RUNNING,
    COMPLETED,
    INVALID
}

object CrawlsTable : UUIDTable() {
    val url = varchar("url", 255)
    val title = varchar("title", 255).nullable().default(null)
    val start = timestamp("start").nullable().default(null)
    val end = timestamp("end").nullable().default(null)
    val type = enumerationByName<CrawlType>("type", 255)
    val error = varchar("error", 255).nullable().default(null)
    val execution = reference("execution", ExecutionsTable)
}

class CrawlEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CrawlEntity>(CrawlsTable)

    var url by CrawlsTable.url
    var title by CrawlsTable.title
    var start by CrawlsTable.start
    var end by CrawlsTable.end
    var type by CrawlsTable.type
    var error by CrawlsTable.error
    var execution by ExecutionEntity referencedOn CrawlsTable.execution
    var parent by CrawlEntity.via(CrawlRelationsTable.child, CrawlRelationsTable.parent)
    var children by CrawlEntity.via(CrawlRelationsTable.parent, CrawlRelationsTable.child)
}

enum class ExecutionType {
    PENDING,
    COMPLETED,
    RUNNING,
}

object ExecutionsTable : UUIDTable() {
    val url = varchar("url", 255)
    val regexp = varchar("regexp", 255)
    val start = timestamp("start")
    val end = timestamp("end").nullable().default(null)
    val type = enumerationByName<ExecutionType>("type", 255)
    val record = reference("record", RecordsTable)
    val rootCrawl = reference("root_crawl", CrawlsTable)
}

class ExecutionEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ExecutionEntity>(ExecutionsTable)

    var url by ExecutionsTable.url
    var regexp by ExecutionsTable.regexp
    var start by ExecutionsTable.start
    var end by ExecutionsTable.end
    var type by ExecutionsTable.type
    var record by RecordEntity referencedOn ExecutionsTable.record
    var rootCrawl by CrawlEntity referencedOn ExecutionsTable.rootCrawl
    val crawls by CrawlEntity referrersOn CrawlsTable.execution

}
