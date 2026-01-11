package ru.application.homemedkit.network.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class UploadLink(
    val href: String
)
