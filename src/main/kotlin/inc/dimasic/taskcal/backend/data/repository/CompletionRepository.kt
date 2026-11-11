package inc.dimasic.taskcal.backend.data.repository

import inc.dimasic.taskcal.backend.data.nowUtc
import inc.dimasic.taskcal.backend.data.table.TaskCompletions
import inc.dimasic.taskcal.backend.domain.model.TaskCompletion
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid

class CompletionRepository(private val db: Database) {

    suspend fun listForTask(userId: Uuid, taskId: Uuid): List<TaskCompletion> = suspendTransaction(db) {
        TaskCompletions.selectAll()
            .where { (TaskCompletions.taskId eq taskId) and (TaskCompletions.userId eq userId) }
            .orderBy(TaskCompletions.occurrenceDate to SortOrder.ASC)
            .map { it.toCompletion() }
    }

    /** Inserts or replaces [completion]; returns false when its id belongs to another user. */
    suspend fun save(userId: Uuid, completion: TaskCompletion): Boolean = suspendTransaction(db) {
        val owner = TaskCompletions.select(TaskCompletions.userId)
            .where { TaskCompletions.id eq completion.id }
            .singleOrNull()
            ?.get(TaskCompletions.userId)
        when (owner) {
            null -> TaskCompletions.insert {
                it[TaskCompletions.id] = completion.id
                it[TaskCompletions.userId] = userId
                it.write(completion)
            }

            userId -> TaskCompletions.update({ TaskCompletions.id eq completion.id }) {
                it.write(completion)
                it[TaskCompletions.updatedAt] = nowUtc()
            }

            else -> return@suspendTransaction false
        }
        true
    }

    suspend fun delete(userId: Uuid, taskId: Uuid, completionId: Uuid): Boolean = suspendTransaction(db) {
        TaskCompletions.deleteWhere {
            (TaskCompletions.id eq completionId) and
                (TaskCompletions.taskId eq taskId) and
                (TaskCompletions.userId eq userId)
        } > 0
    }

    private fun UpdateBuilder<*>.write(completion: TaskCompletion) {
        this[TaskCompletions.taskId] = completion.taskId
        this[TaskCompletions.occurrenceDate] = completion.occurrenceDate
        this[TaskCompletions.completedAt] = completion.completedAt
    }

    private fun ResultRow.toCompletion() = TaskCompletion(
        id = this[TaskCompletions.id],
        taskId = this[TaskCompletions.taskId],
        occurrenceDate = this[TaskCompletions.occurrenceDate],
        completedAt = this[TaskCompletions.completedAt],
    )
}
