package inc.dimasic.taskcal.backend.domain.service

/** Thrown for input that breaks a domain rule; mapped to 400. */
class ValidationException(val errors: List<String>) : RuntimeException(errors.joinToString("; "))

/** Thrown when a resource doesn't exist *for the calling user*; mapped to 404. */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/** Thrown when a request is valid but conflicts with the resource's state; mapped to 409. */
class ConflictException(message: String) : RuntimeException(message)

/** Collects rule violations and throws them together, so a client sees every problem at once. */
internal fun validate(rules: MutableList<String>.() -> Unit) {
    val errors = buildList(rules)
    if (errors.isNotEmpty()) throw ValidationException(errors)
}

internal fun MutableList<String>.check(condition: Boolean, message: () -> String) {
    if (!condition) add(message())
}
