package inc.dimasic.taskcal.backend.api.route

import inc.dimasic.taskcal.backend.api.dto.UserDto
import inc.dimasic.taskcal.backend.api.dto.UserProfileDto
import inc.dimasic.taskcal.backend.api.dto.toDomain
import inc.dimasic.taskcal.backend.api.dto.toDto
import inc.dimasic.taskcal.backend.domain.service.AuthService
import inc.dimasic.taskcal.backend.domain.service.ProfileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.meRoutes(auth: AuthService, profiles: ProfileService) {
    route("/me") {
        get {
            call.respond(UserDto(call.userId.toString()))
        }

        delete {
            auth.deleteAccount(call.userId)
            call.respond(HttpStatusCode.NoContent)
        }

        route("/profile") {
            get {
                call.respond(profiles.get(call.userId).toDto())
            }

            put {
                val profile = call.receive<UserProfileDto>().toDomain()
                call.respond(profiles.save(call.userId, profile).toDto())
            }
        }
    }
}
