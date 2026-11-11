package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.dto.TaskDto
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class IsolationTest {

    @Test
    fun `a user can't see, overwrite or delete another user's task`() = apiTest {
        val alice = newDevice()
        val bob = newDevice()
        val taskId = Uuid.random()
        client.put("/api/v1/tasks/$taskId") { jsonBody(alice, taskJson(title = "Alice's")) }.assertStatus(200)

        client.get("/api/v1/tasks/$taskId") { bearerAuth(bob) }.assertStatus(404)
        client.put("/api/v1/tasks/$taskId") { jsonBody(bob, taskJson(title = "Bob's")) }.assertStatus(404)
        client.delete("/api/v1/tasks/$taskId") { bearerAuth(bob) }.assertStatus(404)
        assertTrue(client.get("/api/v1/tasks") { bearerAuth(bob) }.body<List<TaskDto>>().isEmpty())

        val stillAlices = client.get("/api/v1/tasks/$taskId") { bearerAuth(alice) }.body<TaskDto>()
        assertEquals("Alice's", stillAlices.title)
    }

    @Test
    fun `a user can't add completions to another user's task`() = apiTest {
        val alice = newDevice()
        val bob = newDevice()
        val taskId = Uuid.random()
        client.put("/api/v1/tasks/$taskId") { jsonBody(alice, taskJson(recurrenceType = "DAILY")) }.assertStatus(200)

        client.put("/api/v1/tasks/$taskId/completions/${Uuid.random()}") {
            jsonBody(bob, buildJsonObject {
                put("occurrenceDate", "2026-11-09T07:30:00")
                put("completedAt", "2026-11-09T08:00:00")
            })
        }.assertStatus(404)
        client.get("/api/v1/tasks/$taskId/completions") { bearerAuth(bob) }.assertStatus(404)
    }

    @Test
    fun `deleting the account removes the user and their data`() = apiTest {
        val token = newDevice()
        val taskId = Uuid.random()
        client.put("/api/v1/tasks/$taskId") { jsonBody(token, taskJson()) }.assertStatus(200)

        client.delete("/api/v1/me") { bearerAuth(token) }.assertStatus(204)
        client.get("/api/v1/me") { bearerAuth(token) }.assertStatus(401)

        // The same token can register again, and gets a brand-new, empty account.
        register(token).assertStatus(200)
        assertTrue(client.get("/api/v1/tasks") { bearerAuth(token) }.body<List<TaskDto>>().isEmpty())
    }
}
