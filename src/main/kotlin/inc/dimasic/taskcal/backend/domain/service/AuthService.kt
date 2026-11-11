package inc.dimasic.taskcal.backend.domain.service

import inc.dimasic.taskcal.backend.data.repository.UserRepository
import java.security.MessageDigest
import kotlin.uuid.Uuid

/**
 * v1 auth: the app generates a random token on install and sends it as a bearer token.
 *
 * Only a SHA-256 hash of the token is stored, under provider [DEVICE_PROVIDER]. Google login can
 * later add another provider row for the same user without touching the user's data.
 */
class AuthService(private val users: UserRepository) {

    /** Registers the device token on first call; later calls return the same user. */
    suspend fun registerDevice(token: String): Uuid {
        validateToken(token)
        return users.findOrCreate(DEVICE_PROVIDER, hash(token))
    }

    /** The user this bearer token signs in as, or null when it's unknown or malformed. */
    suspend fun authenticateDevice(token: String): Uuid? {
        if (!isWellFormed(token)) return null
        return users.findUserId(DEVICE_PROVIDER, hash(token))
    }

    suspend fun deleteAccount(userId: Uuid) {
        if (!users.delete(userId)) throw ResourceNotFoundException("User not found")
    }

    private fun validateToken(token: String) = validate {
        check(isWellFormed(token)) {
            "deviceToken must be $MIN_TOKEN_LENGTH-$MAX_TOKEN_LENGTH characters without whitespace"
        }
    }

    private fun isWellFormed(token: String) =
        token.length in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH && token.none { it.isWhitespace() }

    private fun hash(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).toHexString()

    companion object {
        const val DEVICE_PROVIDER = "device"
        const val MIN_TOKEN_LENGTH = 32
        const val MAX_TOKEN_LENGTH = 256
    }
}
