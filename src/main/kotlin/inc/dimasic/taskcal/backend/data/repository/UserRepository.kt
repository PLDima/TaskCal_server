package inc.dimasic.taskcal.backend.data.repository

import inc.dimasic.taskcal.backend.data.table.AuthIdentities
import inc.dimasic.taskcal.backend.data.table.Users
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.uuid.Uuid

class UserRepository(private val db: Database) {

    suspend fun findUserId(provider: String, subject: String): Uuid? = suspendTransaction(db) {
        AuthIdentities.select(AuthIdentities.userId)
            .where { (AuthIdentities.provider eq provider) and (AuthIdentities.subject eq subject) }
            .singleOrNull()
            ?.get(AuthIdentities.userId)
    }

    /** Returns the user behind this identity, creating both on first sight. */
    suspend fun findOrCreate(provider: String, subject: String): Uuid = suspendTransaction(db) {
        AuthIdentities.select(AuthIdentities.userId)
            .where { (AuthIdentities.provider eq provider) and (AuthIdentities.subject eq subject) }
            .singleOrNull()
            ?.get(AuthIdentities.userId)
            ?: Uuid.random().also { newUserId ->
                Users.insert { it[Users.id] = newUserId }
                AuthIdentities.insert {
                    it[AuthIdentities.provider] = provider
                    it[AuthIdentities.subject] = subject
                    it[AuthIdentities.userId] = newUserId
                }
            }
    }

    /** Deletes the user; every row they own goes with them through ON DELETE CASCADE. */
    suspend fun delete(userId: Uuid): Boolean = suspendTransaction(db) {
        Users.deleteWhere { Users.id eq userId } > 0
    }
}
