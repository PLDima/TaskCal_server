package inc.dimasic.taskcal.backend.data.table

import inc.dimasic.taskcal.backend.domain.model.Gender
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object UserProfiles : Table("user_profiles") {
    val userId = uuid("user_id")
    val name = varchar("name", 100)
    val gender = enumerationByName("gender", 16, Gender::class).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val heightCm = integer("height_cm").nullable()
    val stepsGoal = integer("steps_goal")
    val distanceGoalMeters = integer("distance_goal_meters")
    val caloriesBurnedGoal = integer("calories_burned_goal")
    val weightGoal = double("weight_goal")
    val nutritionCalories = integer("nutrition_calories")
    val nutritionProtein = double("nutrition_protein")
    val nutritionCarbs = double("nutrition_carbs")
    val nutritionFat = double("nutrition_fat")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(userId)
}
