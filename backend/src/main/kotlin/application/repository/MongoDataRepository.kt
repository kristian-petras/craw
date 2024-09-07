package application.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import model.WebsiteRecord

class MongoDataRepository(connectionString: String) : DataRepository {
    private val serverApi: ServerApi =
        ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
    private val mongoClientSettings: MongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .serverApi(serverApi)
            .build()

    private val mongoClient = MongoClient.create(mongoClientSettings)
    private val database = mongoClient.getDatabase("craw")
    private val collection = database.getCollection<WebsiteRecord>("websiteRecords")

    override suspend fun getAll(): List<WebsiteRecord> = collection.find().toList()

    override suspend fun upsert(record: WebsiteRecord): Boolean {
        val result = collection.replaceOne(eq("id", record.id), record, ReplaceOptions().upsert(true))
        return result.modifiedCount == 1L
    }

    override suspend fun delete(recordId: Int): Boolean {
        val deletion = collection.deleteOne(eq("id", recordId))
        return deletion.deletedCount == 1L
    }

    override suspend fun get(recordId: Int): WebsiteRecord? = collection.find(eq("id", recordId)).firstOrNull()
}
