package inc.dimasic.taskcal.backend.api.route

import inc.dimasic.taskcal.backend.domain.service.ValidationException
import inc.dimasic.taskcal.backend.plugins.UserPrincipal
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.uuid.Uuid

/** The authenticated caller. Only valid inside an `authenticate { }` block. */
val ApplicationCall.userId: Uuid
    get() = checkNotNull(principal<UserPrincipal>()) { "Route is not behind authentication" }.userId

fun ApplicationCall.uuidParameter(name: String): Uuid {
    val raw = parameters[name]
    return raw?.let(Uuid::parseOrNull) ?: throw ValidationException(listOf("$name must be a UUID, was '$raw'"))
}

fun ApplicationCall.dateQueryParameter(name: String): LocalDate? {
    val raw = request.queryParameters[name] ?: return null
    return try {
        LocalDate.parse(raw)
    } catch (_: DateTimeParseException) {
        throw ValidationException(listOf("$name must be an ISO date (yyyy-MM-dd), was '$raw'"))
    }
}
