package inc.dimasic.taskcal.backend.plugins

import inc.dimasic.taskcal.backend.api.dto.ProblemDto
import inc.dimasic.taskcal.backend.domain.service.ConflictException
import inc.dimasic.taskcal.backend.domain.service.ResourceNotFoundException
import inc.dimasic.taskcal.backend.domain.service.ValidationException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

private const val UNIQUE_VIOLATION = "23505"

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, e ->
            call.respondProblem(HttpStatusCode.BadRequest, "Validation failed", errors = e.errors)
        }
        exception<BadRequestException> { call, e ->
            val detail = generateSequence<Throwable>(e) { it.cause }.last().message
            call.respondProblem(HttpStatusCode.BadRequest, "Malformed request", detail)
        }
        exception<ResourceNotFoundException> { call, e ->
            call.respondProblem(HttpStatusCode.NotFound, "Not found", e.message)
        }
        exception<ConflictException> { call, e ->
            call.respondProblem(HttpStatusCode.Conflict, "Conflict", e.message)
        }
        exception<ExposedSQLException> { call, e ->
            if (e.sqlState == UNIQUE_VIOLATION) {
                call.respondProblem(HttpStatusCode.Conflict, "Conflict", "A record with the same key already exists")
            } else {
                call.application.log.error("Database error", e)
                call.respondProblem(HttpStatusCode.InternalServerError, "Internal server error")
            }
        }
        exception<Throwable> { call, e ->
            call.application.log.error("Unhandled error", e)
            call.respondProblem(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }
}

private suspend fun ApplicationCall.respondProblem(
    status: HttpStatusCode,
    title: String,
    detail: String? = null,
    errors: List<String>? = null,
) {
    val body = AppJson.encodeToString(ProblemDto(title = title, status = status.value, detail = detail, errors = errors))
    respondText(body, ContentType.Application.ProblemJson, status)
}
