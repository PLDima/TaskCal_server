package inc.dimasic.taskcal.backend.api.dto

import inc.dimasic.taskcal.backend.domain.model.MealType
import kotlinx.serialization.Serializable
import java.time.LocalDate

/** [id] comes from the URL on writes; if a body includes it, it must match. */
@Serializable
data class FoodEntryDto(
    val id: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val name: String,
)
