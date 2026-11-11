package inc.dimasic.taskcal.backend.data.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

/** One row per way of signing in as a user: a device token today, a Google account later. */
object AuthIdentities : Table("auth_identities") {
    val provider = varchar("provider", 32)
    val subject = varchar("subject", 255)
    val userId = uuid("user_id")
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(provider, subject)
}
