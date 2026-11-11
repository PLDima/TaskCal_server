package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.dto.FoodEntryDto
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class FoodRoutesTest {

    private fun food(date: String, name: String = "Oatmeal", calories: Int = 350) = buildJsonObject {
        put("date", date)
        put("mealType", "BREAKFAST")
        put("calories", calories)
        put("protein", 12.5)
        put("carbs", 60.0)
        put("fat", 6.0)
        put("name", name)
    }

    @Test
    fun `entries are filtered by inclusive date range and ordered by date`() = apiTest {
        val token = newDevice()
        val dates = listOf("2026-11-09", "2026-11-07", "2026-11-08", "2026-11-10")
        dates.forEach { date ->
            client.put("/api/v1/food-entries/${Uuid.random()}") { jsonBody(token, food(date, name = date)) }
                .assertStatus(200)
        }

        val inRange = client.get("/api/v1/food-entries?from=2026-11-08&to=2026-11-09") { bearerAuth(token) }
            .assertStatus(200)
            .body<List<FoodEntryDto>>()
        assertEquals(listOf("2026-11-08", "2026-11-09"), inRange.map { it.name })

        val all = client.get("/api/v1/food-entries") { bearerAuth(token) }.body<List<FoodEntryDto>>()
        assertEquals(dates.sorted(), all.map { it.name })
    }

    @Test
    fun `put replaces and delete removes an entry`() = apiTest {
        val token = newDevice()
        val id = Uuid.random()
        client.put("/api/v1/food-entries/$id") { jsonBody(token, food("2026-11-08")) }.assertStatus(200)
        client.put("/api/v1/food-entries/$id") { jsonBody(token, food("2026-11-08", calories = 400)) }.assertStatus(200)

        val fetched = client.get("/api/v1/food-entries/$id") { bearerAuth(token) }.body<FoodEntryDto>()
        assertEquals(400, fetched.calories)

        client.delete("/api/v1/food-entries/$id") { bearerAuth(token) }.assertStatus(204)
        client.get("/api/v1/food-entries/$id") { bearerAuth(token) }.assertStatus(404)
    }

    @Test
    fun `invalid entries and ranges are rejected`() = apiTest {
        val token = newDevice()
        client.put("/api/v1/food-entries/${Uuid.random()}") { jsonBody(token, food("2026-11-08", calories = -1)) }
            .assertStatus(400)
        client.get("/api/v1/food-entries?from=2026-11-09&to=2026-11-08") { bearerAuth(token) }.assertStatus(400)
        client.get("/api/v1/food-entries?from=yesterday") { bearerAuth(token) }.assertStatus(400)
    }
}
