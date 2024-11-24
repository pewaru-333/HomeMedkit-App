package ru.application.homemedkit.network.models.bio

import kotlinx.serialization.Serializable

@Serializable
data class ProductProperty(
    val structure: String?,
    val unitVolumeWeight: String?,
    val applicationArea: String?,
    val recommendForUse: String?,
    val storageConditions: String?,
    val releaseForm: String?,
    val quantityInPack: Double?
)
