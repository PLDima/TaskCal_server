package inc.dimasic.taskcal.backend.domain.model

/** Daily intake targets: calories, and the three macros in grams. */
data class NutritionGoal(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)
