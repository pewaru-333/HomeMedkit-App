package ru.application.homemedkit.network.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class Foiv(
    val prodFormNormName: String,
    val prodDNormName: String?,
    val prodPack12: String?,
    val prodPack1Size: String?
)
