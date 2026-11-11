package inc.dimasic.taskcal.backend.domain.service

import inc.dimasic.taskcal.backend.data.repository.ProfileRepository
import inc.dimasic.taskcal.backend.domain.model.UserProfile
import java.time.LocalDate
import kotlin.uuid.Uuid

class ProfileService(private val profiles: ProfileRepository) {

    suspend fun get(userId: Uuid): UserProfile =
        profiles.find(userId) ?: throw ResourceNotFoundException("Profile has not been set")

    suspend fun save(userId: Uuid, profile: UserProfile): UserProfile {
        validate(profile)
        profiles.save(userId, profile)
        return profile
    }

    private fun validate(profile: UserProfile) = validate {
        check(profile.name.length <= 100) { "name must be at most 100 characters" }
        check(profile.dateOfBirth?.isAfter(LocalDate.now()) != true) { "dateOfBirth can't be in the future" }
        check(profile.heightCm == null || profile.heightCm in 30..300) { "heightCm must be between 30 and 300" }
        check(profile.stepsGoal >= 0) { "stepsGoal can't be negative" }
        check(profile.distanceGoalMeters >= 0) { "distanceGoalMeters can't be negative" }
        check(profile.caloriesBurnedGoal >= 0) { "caloriesBurnedGoal can't be negative" }
        check(profile.weightGoal.isFinite() && profile.weightGoal > 0) { "weightGoal must be positive" }
        with(profile.nutritionGoal) {
            check(calories >= 0) { "nutritionGoal.calories can't be negative" }
            check(protein.isNonNegative()) { "nutritionGoal.protein can't be negative" }
            check(carbs.isNonNegative()) { "nutritionGoal.carbs can't be negative" }
            check(fat.isNonNegative()) { "nutritionGoal.fat can't be negative" }
        }
    }
}

internal fun Double.isNonNegative() = isFinite() && this >= 0
