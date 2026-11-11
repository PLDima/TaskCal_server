package inc.dimasic.taskcal.backend.data.table

import inc.dimasic.taskcal.backend.domain.model.MealType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object FoodEntries : Table("food_entries") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val date = date("entry_date")
    val mealType = enumerationByName("meal_type", 16, MealType::class)
    val calories = integer("calories")
    val protein = double("protein")
    val carbs = double("carbs")
    val fat = double("fat")
    val name = varchar("name", 200)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
