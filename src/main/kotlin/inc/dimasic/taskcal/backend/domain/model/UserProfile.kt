package inc.dimasic.taskcal.backend.domain.model

import java.time.LocalDate

/** Mirrors the Android `UserProfile`; personal details are optional, goals are not. */
data class UserProfile(
    val name: String,
    val gender: Gender?,
    val dateOfBirth: LocalDate?,
    val heightCm: Int?,
    val stepsGoal: Int,
    val distanceGoalMeters: Int,
    val caloriesBurnedGoal: Int,
    val weightGoal: Double,
    val nutritionGoal: NutritionGoal,
)
