package inc.dimasic.taskcal.backend.plugins

import inc.dimasic.taskcal.backend.domain.service.AuthService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import kotlin.uuid.Uuid

const val DEVICE_AUTH = "device"

data class UserPrincipal(val userId: Uuid)

/** `Authorization: Bearer <device token>`. Swap or add a provider here when Google login arrives. */
fun Application.configureSecurity(auth: AuthService) {
    install(Authentication) {
        bearer(DEVICE_AUTH) {
            realm = "TaskCal"
            authenticate { credential ->
                auth.authenticateDevice(credential.token)?.let(::UserPrincipal)
            }
        }
    }
}
