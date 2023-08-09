import application.*
import org.http4k.client.JavaHttpClient
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.Instant

private const val port = 8000
fun main() {
    val timeProvider = TimeProvider { Instant.now() }
    val repository = LocalDataRepository()
    val executor = Executor(timeProvider, JavaHttpClient())
    val app = App(executor, repository)
    val server = server(app.getClient()).asServer(Jetty(port)).start()

    readln()
    server.stop()
}
