package inc.dimasic.taskcal.backend.data.repository

import inc.dimasic.taskcal.backend.data.nowUtc
import inc.dimasic.taskcal.backend.data.table.FoodEntries
import inc.dimasic.taskcal.backend.domain.model.FoodEntry
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import kotlin.uuid.Uuid

class FoodRepository(private val db: Database) {

    /** Entries dated within [from]..[to], both inclusive; a null bound leaves that side open. */
    suspend fun list(userId: Uuid, from: LocalDate?, to: LocalDate?): List<FoodEntry> = suspendTransaction(db) {
        var condition: Op<Boolean> = FoodEntries.userId eq userId
        if (from != null) condition = condition and (FoodEntries.date greaterEq from)
        if (to != null) condition = condition and (FoodEntries.date lessEq to)
        FoodEntries.selectAll()
            .where(condition)
            .orderBy(FoodEntries.date to SortOrder.ASC, FoodEntries.createdAt to SortOrder.ASC)
            .map { it.toFoodEntry() }
    }

    suspend fun find(userId: Uuid, entryId: Uuid): FoodEntry? = suspendTransaction(db) {
        FoodEntries.selectAll()
            .where { (FoodEntries.id eq entryId) and (FoodEntries.userId eq userId) }
            .singleOrNull()
            ?.toFoodEntry()
    }

    /** Inserts or replaces [entry]; returns false when its id belongs to another user. */
    suspend fun save(userId: Uuid, entry: FoodEntry): Boolean = suspendTransaction(db) {
        val owner = FoodEntries.select(FoodEntries.userId)
            .where { FoodEntries.id eq entry.id }
            .singleOrNull()
            ?.get(FoodEntries.userId)
        when (owner) {
            null -> FoodEntries.insert {
                it[FoodEntries.id] = entry.id
                it[FoodEntries.userId] = userId
                it.write(entry)
            }

            userId -> FoodEntries.update({ FoodEntries.id eq entry.id }) {
                it.write(entry)
                it[FoodEntries.updatedAt] = nowUtc()
            }

            else -> return@suspendTransaction false
        }
        true
    }

    suspend fun delete(userId: Uuid, entryId: Uuid): Boolean = suspendTransaction(db) {
        FoodEntries.deleteWhere { (FoodEntries.id eq entryId) and (FoodEntries.userId eq userId) } > 0
    }

    private fun UpdateBuilder<*>.write(entry: FoodEntry) {
        this[FoodEntries.date] = entry.date
        this[FoodEntries.mealType] = entry.mealType
        this[FoodEntries.calories] = entry.calories
        this[FoodEntries.protein] = entry.protein
        this[FoodEntries.carbs] = entry.carbs
        this[FoodEntries.fat] = entry.fat
        this[FoodEntries.name] = entry.name
    }

    private fun ResultRow.toFoodEntry() = FoodEntry(
        id = this[FoodEntries.id],
        date = this[FoodEntries.date],
        mealType = this[FoodEntries.mealType],
        calories = this[FoodEntries.calories],
        protein = this[FoodEntries.protein],
        carbs = this[FoodEntries.carbs],
        fat = this[FoodEntries.fat],
        name = this[FoodEntries.name],
    )
}
