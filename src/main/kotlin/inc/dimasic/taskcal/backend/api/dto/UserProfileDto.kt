package inc.dimasic.taskcal.backend.api.dto

import inc.dimasic.taskcal.backend.domain.model.Gender
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class UserProfileDto(
    val name: String = "",
    val gender: Gender? = null,
    @Serializable(with = LocalDateSerializer::class)
    val dateOfBirth: LocalDate? = null,
    val heightCm: Int? = null,
    val stepsGoal: Int,
    val distanceGoalMeters: Int,
    val caloriesBurnedGoal: Int,
    val weightGoal: Double,
    val nutritionGoal: NutritionGoalDto,
)

@Serializable
data class NutritionGoalDto(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)
