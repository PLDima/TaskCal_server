package inc.dimasic.taskcal.backend.api.route

import inc.dimasic.taskcal.backend.api.dto.FoodEntryDto
import inc.dimasic.taskcal.backend.api.dto.toDomain
import inc.dimasic.taskcal.backend.api.dto.toDto
import inc.dimasic.taskcal.backend.domain.service.FoodService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.foodRoutes(food: FoodService) {
    route("/food-entries") {
        get {
            val entries = food.list(
                userId = call.userId,
                from = call.dateQueryParameter("from"),
                to = call.dateQueryParameter("to"),
            )
            call.respond(entries.map { it.toDto() })
        }

        route("/{entryId}") {
            get {
                call.respond(food.get(call.userId, call.uuidParameter("entryId")).toDto())
            }

            put {
                val entry = call.receive<FoodEntryDto>().toDomain(call.uuidParameter("entryId"))
                call.respond(food.save(call.userId, entry).toDto())
            }

            delete {
                food.delete(call.userId, call.uuidParameter("entryId"))
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
