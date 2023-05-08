import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import ru.kosolapov.course.table.Tables

fun Application.configureDatabase() {
    Database.connect(hikari())
    Tables.init()
}

private fun Application.hikari(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.postgresql.Driver"
    config.jdbcUrl = environment.config.property("database.url").getString()
    config.username = environment.config.property("database.user").getString()
    config.password = environment.config.property("database.password").getString()
    config.maximumPoolSize = environment.config.property("database.maximum_pool_size").getString().toInt()
    config.isAutoCommit = false
    config.transactionIsolation = environment.config.property("database.transaction_isolation").getString()
    config.validate()
    return HikariDataSource(config)
}