package ru.application.homemedkit.network.models.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.application.homemedkit.utils.BLANK

@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
) {
    companion object {
        val empty = Token(BLANK, BLANK)
    }
}