import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun h2(): Database {
        return Database.connect(
            url = "jdbc:h2:mem:test",
            user = "root",
            driver = "org.h2.Driver",
            password = ""
        )
    }

    fun postgres(password: String): Database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres",
        user = "postgres",
        password = password,
        driver = "org.postgresql.Driver"
    )
}