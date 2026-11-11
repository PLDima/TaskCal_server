package inc.dimasic.taskcal.backend.api.dto

import inc.dimasic.taskcal.backend.domain.model.FoodEntry
import inc.dimasic.taskcal.backend.domain.model.NutritionGoal
import inc.dimasic.taskcal.backend.domain.model.Task
import inc.dimasic.taskcal.backend.domain.model.TaskCompletion
import inc.dimasic.taskcal.backend.domain.model.UserProfile
import inc.dimasic.taskcal.backend.domain.service.ValidationException
import kotlin.uuid.Uuid

fun TaskDto.toDomain(id: Uuid): Task {
    requireMatchingId("id", this.id, id)
    return Task(
        id = id,
        title = title,
        description = description,
        priority = priority,
        recurrenceType = recurrenceType,
        repeatDayOfWeek = repeatDayOfWeek,
        repeatDayOfMonth = repeatDayOfMonth,
        assignedDateTime = assignedDateTime,
        hasDueTime = hasDueTime,
        isCompleted = isCompleted,
    )
}

fun Task.toDto() = TaskDto(
    id = id.toString(),
    title = title,
    description = description,
    priority = priority,
    recurrenceType = recurrenceType,
    repeatDayOfWeek = repeatDayOfWeek,
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime,
    hasDueTime = hasDueTime,
    isCompleted = isCompleted,
)

fun TaskCompletionDto.toDomain(id: Uuid, taskId: Uuid): TaskCompletion {
    requireMatchingId("id", this.id, id)
    requireMatchingId("taskId", this.taskId, taskId)
    return TaskCompletion(id = id, taskId = taskId, occurrenceDate = occurrenceDate, completedAt = completedAt)
}

fun TaskCompletion.toDto() = TaskCompletionDto(
    id = id.toString(),
    taskId = taskId.toString(),
    occurrenceDate = occurrenceDate,
    completedAt = completedAt,
)

fun FoodEntryDto.toDomain(id: Uuid): FoodEntry {
    requireMatchingId("id", this.id, id)
    return FoodEntry(
        id = id,
        date = date,
        mealType = mealType,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        name = name,
    )
}

fun FoodEntry.toDto() = FoodEntryDto(
    id = id.toString(),
    date = date,
    mealType = mealType,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
)

fun UserProfileDto.toDomain() = UserProfile(
    name = name,
    gender = gender,
    dateOfBirth = dateOfBirth,
    heightCm = heightCm,
    stepsGoal = stepsGoal,
    distanceGoalMeters = distanceGoalMeters,
    caloriesBurnedGoal = caloriesBurnedGoal,
    weightGoal = weightGoal,
    nutritionGoal = with(nutritionGoal) { NutritionGoal(calories, protein, carbs, fat) },
)

fun UserProfile.toDto() = UserProfileDto(
    name = name,
    gender = gender,
    dateOfBirth = dateOfBirth,
    heightCm = heightCm,
    stepsGoal = stepsGoal,
    distanceGoalMeters = distanceGoalMeters,
    caloriesBurnedGoal = caloriesBurnedGoal,
    weightGoal = weightGoal,
    nutritionGoal = with(nutritionGoal) { NutritionGoalDto(calories, protein, carbs, fat) },
)

/** The URL is the source of truth for ids; a body may repeat them, but must not contradict them. */
private fun requireMatchingId(field: String, bodyValue: String?, pathValue: Uuid) {
    if (bodyValue != null && Uuid.parseOrNull(bodyValue) != pathValue) {
        throw ValidationException(listOf("$field in the body doesn't match the URL"))
    }
}
