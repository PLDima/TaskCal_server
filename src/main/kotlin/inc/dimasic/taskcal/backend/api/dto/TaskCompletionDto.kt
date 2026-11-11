package inc.dimasic.taskcal.backend.api.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/** [id] and [taskId] come from the URL on writes; if a body includes them, they must match. */
@Serializable
data class TaskCompletionDto(
    val id: String? = null,
    val taskId: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val occurrenceDate: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val completedAt: LocalDateTime,
)
