package inc.dimasic.taskcal.backend.domain.service

import inc.dimasic.taskcal.backend.data.repository.CompletionRepository
import inc.dimasic.taskcal.backend.data.repository.TaskRepository
import inc.dimasic.taskcal.backend.domain.model.RecurrenceType
import inc.dimasic.taskcal.backend.domain.model.Task
import inc.dimasic.taskcal.backend.domain.model.TaskCompletion
import kotlin.uuid.Uuid

class TaskService(
    private val tasks: TaskRepository,
    private val completions: CompletionRepository,
) {

    suspend fun list(userId: Uuid): List<Task> = tasks.list(userId)

    suspend fun get(userId: Uuid, taskId: Uuid): Task =
        tasks.find(userId, taskId) ?: throw taskNotFound()

    suspend fun save(userId: Uuid, task: Task): Task {
        validate(task)
        if (!tasks.save(userId, task)) throw taskNotFound()
        return task
    }

    suspend fun delete(userId: Uuid, taskId: Uuid) {
        if (!tasks.delete(userId, taskId)) throw taskNotFound()
    }

    suspend fun listCompletions(userId: Uuid, taskId: Uuid): List<TaskCompletion> {
        get(userId, taskId)
        return completions.listForTask(userId, taskId)
    }

    suspend fun saveCompletion(userId: Uuid, completion: TaskCompletion): TaskCompletion {
        val task = get(userId, completion.taskId)
        if (!task.isRepeating) {
            throw ConflictException("Only repeating tasks have completions; set isCompleted on the task instead")
        }
        if (!completions.save(userId, completion)) throw ResourceNotFoundException("Completion not found")
        return completion
    }

    suspend fun deleteCompletion(userId: Uuid, taskId: Uuid, completionId: Uuid) {
        if (!completions.delete(userId, taskId, completionId)) {
            throw ResourceNotFoundException("Completion not found")
        }
    }

    private fun validate(task: Task) = validate {
        check(task.title.isNotBlank()) { "title can't be blank" }
        check(task.title.length <= 200) { "title must be at most 200 characters" }
        check(task.description.length <= 5000) { "description must be at most 5000 characters" }
        check(task.repeatDayOfWeek == null || task.recurrenceType == RecurrenceType.WEEKLY) {
            "repeatDayOfWeek is only allowed for WEEKLY tasks"
        }
        check(task.repeatDayOfMonth == null || task.recurrenceType == RecurrenceType.MONTHLY) {
            "repeatDayOfMonth is only allowed for MONTHLY tasks"
        }
        check(task.repeatDayOfMonth == null || task.repeatDayOfMonth in 1..31) {
            "repeatDayOfMonth must be between 1 and 31"
        }
    }

    private fun taskNotFound() = ResourceNotFoundException("Task not found")
}
