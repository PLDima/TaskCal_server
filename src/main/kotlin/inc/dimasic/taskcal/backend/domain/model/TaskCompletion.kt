package inc.dimasic.taskcal.backend.domain.model

import java.time.LocalDateTime
import kotlin.uuid.Uuid

/** A receipt that one occurrence of a repeating [Task] was completed. */
data class TaskCompletion(
    val id: Uuid,
    val taskId: Uuid,
    val occurrenceDate: LocalDateTime,
    val completedAt: LocalDateTime,
)
