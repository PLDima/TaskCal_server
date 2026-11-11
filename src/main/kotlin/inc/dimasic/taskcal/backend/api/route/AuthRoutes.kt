package inc.dimasic.taskcal.backend.api.route

import inc.dimasic.taskcal.backend.api.dto.DeviceAuthRequest
import inc.dimasic.taskcal.backend.api.dto.UserDto
import inc.dimasic.taskcal.backend.domain.service.AuthService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(auth: AuthService) {
    post("/auth/device") {
        val request = call.receive<DeviceAuthRequest>()
        val userId = auth.registerDevice(request.deviceToken)
        call.respond(UserDto(userId.toString()))
    }
}
