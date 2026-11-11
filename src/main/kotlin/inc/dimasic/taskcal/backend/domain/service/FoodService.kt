package inc.dimasic.taskcal.backend.domain.service

import inc.dimasic.taskcal.backend.data.repository.FoodRepository
import inc.dimasic.taskcal.backend.domain.model.FoodEntry
import java.time.LocalDate
import kotlin.uuid.Uuid

class FoodService(private val food: FoodRepository) {

    suspend fun list(userId: Uuid, from: LocalDate?, to: LocalDate?): List<FoodEntry> {
        validate { check(from == null || to == null || !from.isAfter(to)) { "from must not be after to" } }
        return food.list(userId, from, to)
    }

    suspend fun get(userId: Uuid, entryId: Uuid): FoodEntry =
        food.find(userId, entryId) ?: throw entryNotFound()

    suspend fun save(userId: Uuid, entry: FoodEntry): FoodEntry {
        validate(entry)
        if (!food.save(userId, entry)) throw entryNotFound()
        return entry
    }

    suspend fun delete(userId: Uuid, entryId: Uuid) {
        if (!food.delete(userId, entryId)) throw entryNotFound()
    }

    private fun validate(entry: FoodEntry) = validate {
        check(entry.name.isNotBlank()) { "name can't be blank" }
        check(entry.name.length <= 200) { "name must be at most 200 characters" }
        check(entry.calories >= 0) { "calories can't be negative" }
        check(entry.protein.isNonNegative()) { "protein can't be negative" }
        check(entry.carbs.isNonNegative()) { "carbs can't be negative" }
        check(entry.fat.isNonNegative()) { "fat can't be negative" }
    }

    private fun entryNotFound() = ResourceNotFoundException("Food entry not found")
}
