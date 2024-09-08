package com.craw.app.executor
//
//import com.craw.test.WebsiteRecord
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.test.runCurrent
//import kotlinx.coroutines.test.runTest
//import kotlinx.datetime.Instant
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.Test
//import kotlin.time.Duration
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class GraphAppTest {
//
//    @Test
//    fun `subscribe returns record updates`() = runTest {
//        val app = GraphApp()
//        val updates = mutableListOf<Graph>()
//
//        // - add records before subscription
//        app.upsert(createRecord("snapshot-1"))
//        app.upsert(createRecord("snapshot-2"))
//
//        // - launch subscription
//        val job = launch {
//            app.subscribe().collect {
//                updates.add(it)
//            }
//        }
//        runCurrent()
//
//        // - check that the snapshot is received on subscription
//        assertThat(updates.single().nodes).extracting("recordId").containsExactly("snapshot-1", "snapshot-2")
//
//        // - add updates and check that they are received
//        app.upsert(createRecord("update-1"))
//        runCurrent()
//
//        assertThat(updates).hasSize(2)
//        assertThat(updates.last().nodes).extracting("recordId").containsExactly("snapshot-1", "snapshot-2", "update-1")
//
//        // - update an existing record and check that the update is received
//        app.upsert(createRecord("snapshot-2", url = "http://test.org"))
//        runCurrent()
//
//        assertThat(updates).hasSize(3)
//        assertThat(updates.last().nodes).extracting("recordId").containsExactly("snapshot-1", "snapshot-2", "update-1")
//        assertThat(updates.last().nodes.find { it.recordId == "snapshot-2" }!!.url).isEqualTo("http://test.org")
//
//        // - cancel subscription
//        job.cancel()
//
//        // - retry subscription and check that the snapshot is same as last update
//        val lastUpdate = updates.last()
//
//        val additionalJob = launch {
//            app.subscribe().collect {
//                updates.add(it)
//            }
//        }
//        runCurrent()
//
//        assertThat(updates).hasSize(4)
//        assertThat(updates.last()).isEqualTo(lastUpdate)
//
//        // - remove the record and check that the update is received
//        app.delete("snapshot-1")
//        runCurrent()
//
//        assertThat(updates).hasSize(5)
//        assertThat(updates.last().nodes).extracting("recordId").containsExactly("snapshot-2", "update-1")
//
//        additionalJob.cancel()
//    }
//
//    @Test
//    fun `subscribe returns execution updates`() = runTest {
//        val app = GraphApp()
//        val updates = mutableListOf<Graph>()
//
//        app.upsert(createRecord("record-1"))
//
//        // - launch subscription
//        val job = launch {
//            app.subscribe().collect {
//                updates.add(it)
//            }
//        }
//        runCurrent()
//
//        // - add an execution and check that it is received
//        app.upsert(createExecutionSummary(summaryId = "summary-1", recordId = "record-1"))
//        runCurrent()
//
//        assertThat(updates).hasSize(1)
//        assertThat(updates.single().nodes).extracting("recordId").containsExactly("record-1")
//
//        // - add an execution and check that it is received
//        app.upsert(createRecord("record-2"))
//        runCurrent()
//
//        assertThat(updates).hasSize(2)
//        assertThat(updates.last().nodes).extracting("recordId").containsExactly("record-1", "record-2")
//
//        // - cancel subscription
//        job.cancel()
//    }
//
//    private fun createRecord(
//        recordId: String = "1",
//        label: String = "example",
//        url: String = "http://example.com",
//        regexp: String = ".*",
//        tags: List<String> = emptyList(),
//        active: Boolean = true,
//    ) = WebsiteRecord(
//        recordId = recordId,
//        label = label,
//        url = url,
//        regexp = regexp,
//        tags = tags,
//        active = active,
//    )
//
//    private fun createExecutionSummary(
//        summaryId: String = "1",
//        recordId: String = "1",
//        label: String = "example",
//        active: Boolean = true,
//        start: Instant = Instant.DISTANT_PAST,
//        end: Instant = Instant.DISTANT_FUTURE,
//        executions: List<Execution> = listOf(createExecution()),
//    ) = ExecutionSummary(
//        summaryId = summaryId,
//        recordId = recordId,
//        label = label,
//        active = active,
//        start = start,
//        end = end,
//        executions = executions,
//    )
//
//    private fun createExecution(
//        executionId: String = "1",
//        summaryId: String = "1",
//        url: String = "http://example.com",
//        title: String? = null,
//        crawlTime: Duration = Duration.ZERO,
//        links: List<String> = emptyList(),
//    ) = Execution(
//        executionId = executionId,
//        summaryId = summaryId,
//        url = url,
//        title = title,
//        crawlTime = crawlTime,
//        links = links,
//    )
//}