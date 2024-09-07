class RoutingTest
// internal class RoutingTest {
//    @Test
//    fun `client should be able to show all records`() = runIntegrationTest {
//        // given
//
//        // when
//        val response = getRecords()
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
//        assertThat(response.body<List<WebsiteRecord>>()).isEmpty()
//    }
//
//
//    @Test
//    fun `client should be able to create a new record`() = runIntegrationTest {
//        // given
//
//        // when
//        val response = postRecord()
//        // - check that the new record is present
//        val records = getRecords()
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
//        assertThat(records.body<List<WebsiteRecord>>())
//            .extracting("url", "boundaryRegExp", "periodicity", "label", "active", "tags")
//            .containsExactly(tuple("1", ".*", "PT1M", "test-label", false, emptyList<String>()))
//    }
//
//    @Test
//    fun `client should be able to modify the record`() = runIntegrationTest {
//        // given
//        // - stored record
//        val id = postRecord().body<Int>()
//
//        // when
//        val response = modifyRecord(id)
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
//    }
//
//
//    @Test
//    fun `client should not be able to modify non existing record`() = runIntegrationTest {
//        // given
//
//        // when
//        val response = modifyRecord(1)
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
//    }
//
//    @Test
//    fun `client should be able to delete existing record`() = runIntegrationTest {
//        // given
//        val recordId = postRecord().body<Int>()
//
//        // when
//        val response = deleteRecord(recordId)
//        val records = getRecords()
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
//        assertThat(records.body<List<WebsiteRecord>>()).isEmpty()
//    }
//
//
//    @Test
//    fun `client should not be able to delete not existing record`() = runIntegrationTest {
//        // given
//
//        // when
//        val response = deleteRecord(1)
//
//        // then
//        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
//    }
//
//    private suspend fun HttpClient.getRecords(): HttpResponse = get("/records")
//    private suspend fun HttpClient.postRecord(): HttpResponse = post("/record") {
//        contentType(ContentType.parse("application/json"))
//        setBody(WebsiteRecordAdd("1", ".*", "PT1M", "test-label", false, emptyList()))
//    }
//    private suspend fun HttpClient.modifyRecord(id: Int): HttpResponse = put("/record") {
//        contentType(ContentType.parse("application/json"))
//        setBody(WebsiteRecordModify(id, "2", ".*", "PT1M", "test-label", false, emptyList()))
//    }
//    private suspend fun HttpClient.deleteRecord(id: Int): HttpResponse = delete("/record") {
//        contentType(ContentType.parse("application/json"))
//        setBody(WebsiteRecordDelete(id))
//    }
//
//
//    private fun runIntegrationTest(block: suspend HttpClient.() -> Unit) = testApplication {
//        environment {
//            config = ApplicationConfig("application-test.conf")
//        }
//        val client = createClient {
//            install(ContentNegotiation) {
//                json()
//            }
//        }
//        block(client)
//    }
// }
