import application.app
import org.http4k.server.Jetty
import org.http4k.server.asServer

private const val port = 8080
fun main() {
    val repository = TODO()
    val server = app(repository).asServer(Jetty(port)).start()
    server.stop()
}
