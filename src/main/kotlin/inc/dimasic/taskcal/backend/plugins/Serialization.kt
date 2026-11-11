package inc.dimasic.taskcal.backend.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

/** Lenient on input (unknown fields are ignored so older servers accept newer clients). */
val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(AppJson)
    }
}
