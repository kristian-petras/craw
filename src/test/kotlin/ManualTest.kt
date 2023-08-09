
import org.http4k.client.JavaHttpClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("Used for manual testing.")
class ManualTest {

    @Test
    fun getRecords() {
        val request = Request(Method.GET, "localhost:8000/records")

        val client = JavaHttpClient()

        println(client(request))
    }
}