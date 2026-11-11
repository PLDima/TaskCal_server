package inc.dimasic.taskcal.backend.api.dto

import kotlinx.serialization.Serializable

/** Error body in the shape of RFC 9457 problem details, plus every validation message. */
@Serializable
data class ProblemDto(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String? = null,
    val errors: List<String>? = null,
)
