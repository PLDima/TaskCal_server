package inc.dimasic.taskcal.backend.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.Closeable
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class DatabaseConfig(
    val url: String,
    val user: String?,
    val password: String?,
    val maxPoolSize: Int,
) {
    companion object {
        fun from(config: ApplicationConfig) = DatabaseConfig(
            url = config.property("database.url").getString(),
            user = config.propertyOrNull("database.user")?.getString()?.takeIf { it.isNotBlank() },
            password = config.propertyOrNull("database.password")?.getString()?.takeIf { it.isNotBlank() },
            maxPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toInt() ?: 10,
        )
    }
}

/** The pooled connection, migrated to the latest schema. Close it when the application stops. */
class AppDatabase private constructor(
    private val dataSource: HikariDataSource,
    val database: Database,
) : Closeable {
    override fun close() = dataSource.close()

    companion object {
        fun connect(config: DatabaseConfig): AppDatabase {
            val dataSource = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = config.url
                    config.user?.let { username = it }
                    config.password?.let { password = it }
                    maximumPoolSize = config.maxPoolSize
                },
            )
            try {
                Flyway.configure().dataSource(dataSource).load().migrate()
            } catch (e: Exception) {
                dataSource.close()
                throw e
            }
            return AppDatabase(dataSource, Database.connect(dataSource))
        }
    }
}

internal fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
