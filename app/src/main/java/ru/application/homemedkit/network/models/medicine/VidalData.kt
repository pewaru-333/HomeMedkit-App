package ru.application.homemedkit.network.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class VidalData(
    val phKinetics: String?,
    val images: List<String>?
)