package inc.dimasic.taskcal.backend.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Mirrors the Android `Task`. Repeating tasks don't store their occurrences — the app expands
 * them — and completing one occurrence is recorded as a [TaskCompletion] instead of [isCompleted].
 */
data class Task(
    val id: Uuid,
    val title: String,
    val description: String,
    val priority: Priority,
    val recurrenceType: RecurrenceType,
    val repeatDayOfWeek: DayOfWeek?,
    val repeatDayOfMonth: Int?,
    val assignedDateTime: LocalDateTime?,
    val hasDueTime: Boolean,
    val isCompleted: Boolean,
) {
    val isRepeating: Boolean get() = recurrenceType != RecurrenceType.NONE
}
