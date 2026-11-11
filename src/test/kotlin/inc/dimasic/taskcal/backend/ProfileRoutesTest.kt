package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.dto.UserProfileDto
import inc.dimasic.taskcal.backend.domain.model.Gender
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileRoutesTest {

    private fun profile(heightCm: Int = 180): JsonObject = buildJsonObject {
        put("name", "Dmytro")
        put("gender", "MALE")
        put("dateOfBirth", "1995-04-12")
        put("heightCm", heightCm)
        put("stepsGoal", 10_000)
        put("distanceGoalMeters", 5_000)
        put("caloriesBurnedGoal", 2_500)
        put("weightGoal", 68.0)
        putJsonObject("nutritionGoal") {
            put("calories", 3000)
            put("protein", 140.0)
            put("carbs", 250.0)
            put("fat", 70.0)
        }
    }

    @Test
    fun `profile is 404 until set, then round-trips`() = apiTest {
        val token = newDevice()
        client.get("/api/v1/me/profile") { bearerAuth(token) }.assertStatus(404)

        client.put("/api/v1/me/profile") { jsonBody(token, profile()) }.assertStatus(200)
        client.put("/api/v1/me/profile") { jsonBody(token, profile(heightCm = 181)) }.assertStatus(200)

        val saved = client.get("/api/v1/me/profile") { bearerAuth(token) }.assertStatus(200).body<UserProfileDto>()
        assertEquals(181, saved.heightCm)
        assertEquals(Gender.MALE, saved.gender)
        assertEquals(LocalDate.of(1995, 4, 12), saved.dateOfBirth)
        assertEquals(140.0, saved.nutritionGoal.protein)
    }

    @Test
    fun `invalid profile is rejected`() = apiTest {
        val token = newDevice()
        client.put("/api/v1/me/profile") { jsonBody(token, profile(heightCm = 5)) }.assertStatus(400)
    }
}
