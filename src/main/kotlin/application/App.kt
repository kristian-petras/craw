package application

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import model.WebsiteRecord
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

typealias GetAwaitable = CompletableDeferred<List<WebsiteRecord>>

typealias AddAwaitable = Pair<CompletableDeferred<Unit>, WebsiteRecord>

typealias ModifyAwaitable = Pair<CompletableDeferred<Boolean>, WebsiteRecord>
typealias DeleteAwaitable = Pair<CompletableDeferred<Boolean>, WebsiteRecord>

class App(private val executor: Executor, private val repository: DataRepository) {
    inner class Client internal constructor(
        private val get: SendChannel<GetAwaitable>,
        private val add: SendChannel<AddAwaitable>,
        private val modify: SendChannel<ModifyAwaitable>,
        private val delete: SendChannel<DeleteAwaitable>
    ) {
        suspend fun add(record: WebsiteRecord) {
            val payload = CompletableDeferred<Unit>() to record
            add.send(payload)
            payload.first.await()
        }

        suspend fun modify(record: WebsiteRecord): Boolean {
            val payload = CompletableDeferred<Boolean>() to record
            modify.send(payload)
            return payload.first.await()
        }

        suspend fun delete(record: WebsiteRecord): Boolean {
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

    fun getClient() : Client = Client(getChannel, addChannel, modifyChannel, deleteChannel)

    private val getChannel = Channel<GetAwaitable>(Channel.UNLIMITED)
    private val addChannel = Channel<AddAwaitable>(Channel.UNLIMITED)
    private val modifyChannel = Channel<ModifyAwaitable>(Channel.UNLIMITED)
    private val deleteChannel = Channel<DeleteAwaitable>(Channel.UNLIMITED)
    suspend fun run() = coroutineScope {
        val executionChannel = executor.subscribe().produceIn(this)

        while (isActive) {
            select {
                executionChannel.onReceive {
                    repository.add(it)
                    val record = repository.getRecord(it.recordId)!!
                    executor.schedule(record, it.lastExecutionTimestamp + record.periodicity)
                }
                getChannel.onReceive {
                    val records = repository.getAll()
                    it.complete(records)
                }
                addChannel.onReceive { (result, record) ->
                    repository.add(record)
                    result.complete(Unit)
                }
                modifyChannel.onReceive { (result, record) ->
                    val success = repository.modify(record)
                    result.complete(success)
                }
                deleteChannel.onReceive { (result, record) ->
                    val success = repository.delete(record)
                    result.complete(success)
                }
            }

            println("select!!")
        }
        println("ending!!")
    }

}

private operator fun Instant.plus(duration: Duration): Instant = this.plus(duration.toJavaDuration())
