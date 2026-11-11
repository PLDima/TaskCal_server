package inc.dimasic.taskcal.backend.api.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/** ISO-8601 `2026-11-08`. */
object LocalDateSerializer : StringSerializer<LocalDate>("LocalDate", LocalDate::parse)

/** ISO-8601 without a zone, e.g. `2026-11-08T09:30:00` — the same form the app stores in Room. */
object LocalDateTimeSerializer : StringSerializer<LocalDateTime>("LocalDateTime", LocalDateTime::parse)

/** Day name, e.g. `MONDAY`. */
object DayOfWeekSerializer : StringSerializer<DayOfWeek>("DayOfWeek", { DayOfWeek.valueOf(it.uppercase()) }, { it.name })

/** Serializes a value as a string; parse failures become [SerializationException], i.e. a 400. */
abstract class StringSerializer<T : Any>(
    private val name: String,
    private val parse: (String) -> T,
    private val format: (T) -> String = { it.toString() },
) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("inc.dimasic.taskcal.$name", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(format(value))

    override fun deserialize(decoder: Decoder): T {
        val text = decoder.decodeString()
        return try {
            parse(text)
        } catch (e: Exception) {
            throw SerializationException("'$text' is not a valid $name", e)
        }
    }
}
