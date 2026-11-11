package inc.dimasic.taskcal.backend.api.route

import inc.dimasic.taskcal.backend.api.dto.TaskCompletionDto
import inc.dimasic.taskcal.backend.api.dto.TaskDto
import inc.dimasic.taskcal.backend.api.dto.toDomain
import inc.dimasic.taskcal.backend.api.dto.toDto
import inc.dimasic.taskcal.backend.domain.service.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoutes(tasks: TaskService) {
    route("/tasks") {
        get {
            call.respond(tasks.list(call.userId).map { it.toDto() })
        }

        route("/{taskId}") {
            get {
                call.respond(tasks.get(call.userId, call.uuidParameter("taskId")).toDto())
            }

            put {
                val task = call.receive<TaskDto>().toDomain(call.uuidParameter("taskId"))
                call.respond(tasks.save(call.userId, task).toDto())
            }

            delete {
                tasks.delete(call.userId, call.uuidParameter("taskId"))
                call.respond(HttpStatusCode.NoContent)
            }

            route("/completions") {
                get {
                    val completions = tasks.listCompletions(call.userId, call.uuidParameter("taskId"))
                    call.respond(completions.map { it.toDto() })
                }

                put("/{completionId}") {
                    val completion = call.receive<TaskCompletionDto>().toDomain(
                        id = call.uuidParameter("completionId"),
                        taskId = call.uuidParameter("taskId"),
                    )
                    call.respond(tasks.saveCompletion(call.userId, completion).toDto())
                }

                delete("/{completionId}") {
                    tasks.deleteCompletion(
                        userId = call.userId,
                        taskId = call.uuidParameter("taskId"),
                        completionId = call.uuidParameter("completionId"),
                    )
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}
