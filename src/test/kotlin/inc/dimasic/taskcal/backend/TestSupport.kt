package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.plugins.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

/** Runs [block] against the full application on a fresh in-memory H2 database (PostgreSQL mode). */
fun apiTest(block: suspend ApiTest.() -> Unit) = testApplication {
    environment {
        config = MapApplicationConfig(
            "database.url" to "jdbc:h2:mem:${Uuid.random()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;" +
                "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
            "database.user" to "sa",
            "database.password" to "",
            "database.maxPoolSize" to "2",
        )
    }
    application { module() }
    ApiTest(this).block()
}

class ApiTest(builder: ApplicationTestBuilder) {
    val client: HttpClient = builder.createClient {
        install(ContentNegotiation) { json(AppJson) }
    }

    suspend fun register(token: String): HttpResponse = client.post("/api/v1/auth/device") {
        contentType(ContentType.Application.Json)
        setBody(buildJsonObject { put("deviceToken", token) })
    }

    /** Registers a fresh device and returns its token. */
    suspend fun newDevice(): String = randomToken().also { register(it).assertStatus(200) }
}

fun randomToken() = "${Uuid.random()}-${Uuid.random()}"

fun HttpRequestBuilder.jsonBody(token: String, body: JsonObject) {
    bearerAuth(token)
    contentType(ContentType.Application.Json)
    setBody(body)
}

fun taskJson(
    title: String = "Morning run",
    recurrenceType: String = "NONE",
    extra: Map<String, Any?> = emptyMap(),
) = buildJsonObject {
    put("title", title)
    put("description", "5 km")
    put("priority", "HIGH")
    put("recurrenceType", recurrenceType)
    put("assignedDateTime", "2026-11-09T07:30:00")
    put("hasDueTime", true)
    extra.forEach { (key, value) ->
        when (value) {
            null -> put(key, null as String?)
            is Number -> put(key, value)
            is Boolean -> put(key, value)
            else -> put(key, value.toString())
        }
    }
}

suspend fun HttpResponse.assertStatus(expected: Int): HttpResponse = also {
    if (status.value != expected) {
        val body = runCatching { bodyAsText() }.getOrDefault("<unreadable>")
        assertEquals(expected, status.value, "Unexpected status; body: $body")
    }
}
