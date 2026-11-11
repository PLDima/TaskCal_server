package inc.dimasic.taskcal.backend.domain.model

import java.time.LocalDate
import kotlin.uuid.Uuid

data class FoodEntry(
    val id: Uuid,
    val date: LocalDate,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val name: String,
)
