import application.LocalDataRepository
import application.app
import org.http4k.server.Jetty
import org.http4k.server.asServer

private const val port = 8000
fun main() {
    val repository = LocalDataRepository()
    val server = app(repository).asServer(Jetty(port)).start()

    readln()
    server.stop()
}
