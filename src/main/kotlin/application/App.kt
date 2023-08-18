package application

import application.repository.DataRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import model.*
import utility.TimeProvider
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.toJavaDuration

typealias GetAwaitable = CompletableDeferred<List<WebsiteRecord>>

typealias AddAwaitable = Pair<CompletableDeferred<Int>, WebsiteRecordAdd>

typealias ModifyAwaitable = Pair<CompletableDeferred<Boolean>, WebsiteRecordModify>
typealias DeleteAwaitable = Pair<CompletableDeferred<Boolean>, WebsiteRecordDelete>

class App(
    private val executor: Executor,
    private val repository: DataRepository,
    private val timeProvider: TimeProvider
) {
    inner class Client internal constructor(
        private val get: SendChannel<GetAwaitable>,
        private val add: SendChannel<AddAwaitable>,
        private val modify: SendChannel<ModifyAwaitable>,
        private val delete: SendChannel<DeleteAwaitable>
    ) {
        suspend fun add(record: WebsiteRecordAdd): Int {
            val payload = CompletableDeferred<Int>() to record
            add.send(payload)
            return payload.first.await()
        }

        suspend fun modify(record: WebsiteRecordModify): Boolean {
            val payload = CompletableDeferred<Boolean>() to record
            modify.send(payload)
            return payload.first.await()
        }

        suspend fun delete(record: WebsiteRecordDelete): Boolean {
            val payload = CompletableDeferred<Boolean>() to record
            delete.send(payload)
            return payload.first.await()
        }

        suspend fun getAll(): List<WebsiteRecord> {
            val records = CompletableDeferred<List<WebsiteRecord>>()
            get.send(records)
            return records.await()
        }
    }

    fun getClient(): Client = Client(getChannel, addChannel, modifyChannel, deleteChannel)

    private val counter = AtomicInteger()
    private val getChannel = Channel<GetAwaitable>(Channel.UNLIMITED)
    private val addChannel = Channel<AddAwaitable>(Channel.UNLIMITED)
    private val modifyChannel = Channel<ModifyAwaitable>(Channel.UNLIMITED)
    private val deleteChannel = Channel<DeleteAwaitable>(Channel.UNLIMITED)
    suspend fun run() = coroutineScope {
        val executionChannel = executor.subscribe().produceIn(this)

        while (isActive) {
            select<Unit> {
                executionChannel.onReceive { (recordId, execution) ->
                    val timestamp = timeProvider.now()
                    val record = repository.get(recordId)!!
                    val updatedRecord = record.copy(
                        executions = record.executions + execution,
                        lastExecutionTimestamp = timestamp,
                        lastExecutionStatus = false
                    )
                    repository.upsert(updatedRecord)
                    schedule(updatedRecord)
                }
                getChannel.onReceive {
                    val records = repository.getAll()
                    it.complete(records)
                }
                addChannel.onReceive { (result, new) ->
                    val id = counter.getAndIncrement()
                    val record = WebsiteRecord(
                        id = id,
                        url = new.url,
                        boundaryRegExp = new.boundaryRegExp,
                        periodicity = new.periodicity,
                        label = new.label,
                        active = new.active,
                        tags = new.tags,
                        executions = emptyList(),
                        lastExecutionTimestamp = null,
                        lastExecutionStatus = null
                    )
                    repository.upsert(record)
                    if (record.active) {
                        executor.schedule(record.toExecutorSearch(), timeProvider.now())
                    }
                    result.complete(id)
                }
                modifyChannel.onReceive { (result, modify) ->
                    val oldRecord = repository.get(modify.id)

                    if (oldRecord == null) {
                        result.complete(false)
                        return@onReceive
                    }
                    executor.remove(oldRecord.id)

                    val newRecord = modify.from(oldRecord)
                    schedule(newRecord)

                    val success = repository.upsert(newRecord)
                    result.complete(success)
                }
                deleteChannel.onReceive { (result, delete) ->
                    executor.remove(delete.id)
                    val success = repository.delete(delete.id)
                    result.complete(success)
                }
            }

            println("select!!")
        }
        println("ending!!")
    }

    private fun schedule(record: WebsiteRecord) {
        if (record.active) {
            val timestamp = if (record.lastExecutionTimestamp == null)
                timeProvider.now()
            else
                record.lastExecutionTimestamp + Duration.parse(record.periodicity)

            executor.schedule(record.toExecutorSearch(), timestamp)
        }
    }
}

fun WebsiteRecord.toExecutorSearch() = ExecutorSearch(id, url, boundaryRegExp)

private operator fun Instant.plus(duration: Duration): Instant = this.plus(duration.toJavaDuration())
