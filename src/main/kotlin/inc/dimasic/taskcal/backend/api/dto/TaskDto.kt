package inc.dimasic.taskcal.backend.api.dto

import inc.dimasic.taskcal.backend.domain.model.Priority
import inc.dimasic.taskcal.backend.domain.model.RecurrenceType
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDateTime

/** [id] comes from the URL on writes; if a body includes it, it must match. */
@Serializable
data class TaskDto(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val priority: Priority,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    @Serializable(with = DayOfWeekSerializer::class)
    val repeatDayOfWeek: DayOfWeek? = null,
    val repeatDayOfMonth: Int? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val assignedDateTime: LocalDateTime? = null,
    val hasDueTime: Boolean = false,
    val isCompleted: Boolean = false,
)
