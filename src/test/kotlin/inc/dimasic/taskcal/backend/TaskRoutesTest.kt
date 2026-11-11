package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.dto.ProblemDto
import inc.dimasic.taskcal.backend.api.dto.TaskCompletionDto
import inc.dimasic.taskcal.backend.api.dto.TaskDto
import inc.dimasic.taskcal.backend.domain.model.Priority
import inc.dimasic.taskcal.backend.domain.model.RecurrenceType
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class TaskRoutesTest {

    @Test
    fun `put creates, then replaces, a task`() = apiTest {
        val token = newDevice()
        val id = Uuid.random()

        val created = client.put("/api/v1/tasks/$id") { jsonBody(token, taskJson()) }.assertStatus(200).body<TaskDto>()
        assertEquals(id.toString(), created.id)
        assertEquals(Priority.HIGH, created.priority)
        assertEquals(LocalDateTime.of(2026, 11, 9, 7, 30), created.assignedDateTime)

        client.put("/api/v1/tasks/$id") {
            jsonBody(token, taskJson(title = "Evening run", extra = mapOf("isCompleted" to true)))
        }.assertStatus(200)

        val fetched = client.get("/api/v1/tasks/$id") { bearerAuth(token) }.assertStatus(200).body<TaskDto>()
        assertEquals("Evening run", fetched.title)
        assertTrue(fetched.isCompleted)

        val all = client.get("/api/v1/tasks") { bearerAuth(token) }.body<List<TaskDto>>()
        assertEquals(listOf(id.toString()), all.map { it.id })
    }

    @Test
    fun `weekly task round-trips its weekday`() = apiTest {
        val token = newDevice()
        val id = Uuid.random()
        val task = taskJson(recurrenceType = "WEEKLY", extra = mapOf("repeatDayOfWeek" to "FRIDAY"))

        val saved = client.put("/api/v1/tasks/$id") { jsonBody(token, task) }.assertStatus(200).body<TaskDto>()
        assertEquals(RecurrenceType.WEEKLY, saved.recurrenceType)
        assertEquals(DayOfWeek.FRIDAY, saved.repeatDayOfWeek)
    }

    @Test
    fun `delete removes the task`() = apiTest {
        val token = newDevice()
        val id = Uuid.random()
        client.put("/api/v1/tasks/$id") { jsonBody(token, taskJson()) }.assertStatus(200)

        client.delete("/api/v1/tasks/$id") { bearerAuth(token) }.assertStatus(204)
        client.get("/api/v1/tasks/$id") { bearerAuth(token) }.assertStatus(404)
        client.delete("/api/v1/tasks/$id") { bearerAuth(token) }.assertStatus(404)
    }

    @Test
    fun `invalid tasks are rejected with every reason`() = apiTest {
        val token = newDevice()
        val invalid = taskJson(
            title = " ",
            recurrenceType = "DAILY",
            extra = mapOf("repeatDayOfMonth" to 40),
        )
        val problem = client.put("/api/v1/tasks/${Uuid.random()}") { jsonBody(token, invalid) }
            .assertStatus(400)
            .body<ProblemDto>()
        assertEquals(3, problem.errors?.size, "errors: ${problem.errors}")
    }

    @Test
    fun `malformed input is a 400, not a 500`() = apiTest {
        val token = newDevice()
        client.get("/api/v1/tasks/not-a-uuid") { bearerAuth(token) }.assertStatus(400)
        client.put("/api/v1/tasks/${Uuid.random()}") {
            jsonBody(token, taskJson(extra = mapOf("assignedDateTime" to "tomorrow")))
        }.assertStatus(400)
        client.put("/api/v1/tasks/${Uuid.random()}") {
            jsonBody(token, taskJson(extra = mapOf("priority" to "URGENT")))
        }.assertStatus(400)
        client.put("/api/v1/tasks/${Uuid.random()}") {
            jsonBody(token, taskJson(extra = mapOf("id" to Uuid.random().toString())))
        }.assertStatus(400)
    }

    @Test
    fun `completions are recorded for repeating tasks only`() = apiTest {
        val token = newDevice()
        val repeating = Uuid.random()
        val oneOff = Uuid.random()
        client.put("/api/v1/tasks/$repeating") { jsonBody(token, taskJson(recurrenceType = "DAILY")) }.assertStatus(200)
        client.put("/api/v1/tasks/$oneOff") { jsonBody(token, taskJson()) }.assertStatus(200)

        val completion = buildJsonObject {
            put("occurrenceDate", "2026-11-09T07:30:00")
            put("completedAt", "2026-11-09T08:05:00")
        }
        val completionId = Uuid.random()
        val saved = client.put("/api/v1/tasks/$repeating/completions/$completionId") { jsonBody(token, completion) }
            .assertStatus(200)
            .body<TaskCompletionDto>()
        assertEquals(repeating.toString(), saved.taskId)

        client.put("/api/v1/tasks/$oneOff/completions/${Uuid.random()}") { jsonBody(token, completion) }
            .assertStatus(409)

        // A second receipt for the same occurrence is a conflict, not a duplicate.
        client.put("/api/v1/tasks/$repeating/completions/${Uuid.random()}") { jsonBody(token, completion) }
            .assertStatus(409)

        val listed = client.get("/api/v1/tasks/$repeating/completions") { bearerAuth(token) }
            .body<List<TaskCompletionDto>>()
        assertEquals(listOf(completionId.toString()), listed.map { it.id })

        client.delete("/api/v1/tasks/$repeating/completions/$completionId") { bearerAuth(token) }.assertStatus(204)
        val afterDelete = client.get("/api/v1/tasks/$repeating/completions") { bearerAuth(token) }
            .body<List<TaskCompletionDto>>()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `deleting a task deletes its completions`() = apiTest {
        val token = newDevice()
        val taskId = Uuid.random()
        val completionId = Uuid.random()
        client.put("/api/v1/tasks/$taskId") { jsonBody(token, taskJson(recurrenceType = "DAILY")) }.assertStatus(200)
        client.put("/api/v1/tasks/$taskId/completions/$completionId") {
            jsonBody(token, buildJsonObject {
                put("occurrenceDate", "2026-11-09T07:30:00")
                put("completedAt", "2026-11-09T08:05:00")
            })
        }.assertStatus(200)

        client.delete("/api/v1/tasks/$taskId") { bearerAuth(token) }.assertStatus(204)

        // Recreating the task with the same id starts with no completions.
        client.put("/api/v1/tasks/$taskId") { jsonBody(token, taskJson(recurrenceType = "DAILY")) }.assertStatus(200)
        val completions = client.get("/api/v1/tasks/$taskId/completions") { bearerAuth(token) }
            .body<List<TaskCompletionDto>>()
        assertTrue(completions.isEmpty())
    }
}
