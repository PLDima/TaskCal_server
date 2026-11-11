package inc.dimasic.taskcal.backend.data.repository

import inc.dimasic.taskcal.backend.data.nowUtc
import inc.dimasic.taskcal.backend.data.table.UserProfiles
import inc.dimasic.taskcal.backend.domain.model.NutritionGoal
import inc.dimasic.taskcal.backend.domain.model.UserProfile
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid

class ProfileRepository(private val db: Database) {

    suspend fun find(userId: Uuid): UserProfile? = suspendTransaction(db) {
        UserProfiles.selectAll().where { UserProfiles.userId eq userId }.singleOrNull()?.toProfile()
    }

    suspend fun save(userId: Uuid, profile: UserProfile): Unit = suspendTransaction(db) {
        val updated = UserProfiles.update({ UserProfiles.userId eq userId }) {
            it.write(profile)
            it[UserProfiles.updatedAt] = nowUtc()
        }
        if (updated == 0) {
            UserProfiles.insert {
                it[UserProfiles.userId] = userId
                it.write(profile)
            }
        }
    }

    private fun UpdateBuilder<*>.write(profile: UserProfile) {
        this[UserProfiles.name] = profile.name
        this[UserProfiles.gender] = profile.gender
        this[UserProfiles.dateOfBirth] = profile.dateOfBirth
        this[UserProfiles.heightCm] = profile.heightCm
        this[UserProfiles.stepsGoal] = profile.stepsGoal
        this[UserProfiles.distanceGoalMeters] = profile.distanceGoalMeters
        this[UserProfiles.caloriesBurnedGoal] = profile.caloriesBurnedGoal
        this[UserProfiles.weightGoal] = profile.weightGoal
        this[UserProfiles.nutritionCalories] = profile.nutritionGoal.calories
        this[UserProfiles.nutritionProtein] = profile.nutritionGoal.protein
        this[UserProfiles.nutritionCarbs] = profile.nutritionGoal.carbs
        this[UserProfiles.nutritionFat] = profile.nutritionGoal.fat
    }

    private fun ResultRow.toProfile() = UserProfile(
        name = this[UserProfiles.name],
        gender = this[UserProfiles.gender],
        dateOfBirth = this[UserProfiles.dateOfBirth],
        heightCm = this[UserProfiles.heightCm],
        stepsGoal = this[UserProfiles.stepsGoal],
        distanceGoalMeters = this[UserProfiles.distanceGoalMeters],
        caloriesBurnedGoal = this[UserProfiles.caloriesBurnedGoal],
        weightGoal = this[UserProfiles.weightGoal],
        nutritionGoal = NutritionGoal(
            calories = this[UserProfiles.nutritionCalories],
            protein = this[UserProfiles.nutritionProtein],
            carbs = this[UserProfiles.nutritionCarbs],
            fat = this[UserProfiles.nutritionFat],
        ),
    )
}
