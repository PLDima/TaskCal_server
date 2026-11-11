package inc.dimasic.taskcal.backend.data.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object TaskCompletions : Table("task_completions") {
    val id = uuid("id")
    val taskId = uuid("task_id")
    val userId = uuid("user_id")
    val occurrenceDate = datetime("occurrence_date")
    val completedAt = datetime("completed_at")
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
