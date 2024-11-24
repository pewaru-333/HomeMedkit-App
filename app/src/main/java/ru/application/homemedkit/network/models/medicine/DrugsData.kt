package ru.application.homemedkit.network.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class DrugsData(
    val prodDescLabel: String,
    val foiv: Foiv,
    val expireDate: Long,
    val vidalData: VidalData?
)
