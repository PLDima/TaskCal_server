package inc.dimasic.taskcal.backend.data.table

import inc.dimasic.taskcal.backend.domain.model.Priority
import inc.dimasic.taskcal.backend.domain.model.RecurrenceType
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import java.time.DayOfWeek

object Tasks : Table("tasks") {
    val id = uuid("id")
    val userId = uuid("user_id")
    val title = varchar("title", 200)
    val description = varchar("description", 5000)
    val priority = enumerationByName("priority", 16, Priority::class)
    val recurrenceType = enumerationByName("recurrence_type", 16, RecurrenceType::class)
    val repeatDayOfWeek = enumerationByName("repeat_day_of_week", 16, DayOfWeek::class).nullable()
    val repeatDayOfMonth = integer("repeat_day_of_month").nullable()
    val assignedDateTime = datetime("assigned_date_time").nullable()
    val hasDueTime = bool("has_due_time")
    val isCompleted = bool("is_completed")
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
