package inc.dimasic.taskcal.backend.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceAuthRequest(val deviceToken: String)

@Serializable
data class UserDto(val userId: String)
