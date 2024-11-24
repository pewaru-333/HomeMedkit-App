package ru.application.homemedkit.network.models.bio

import kotlinx.serialization.Serializable

@Serializable
data class BioData(
    val productName: String,
    val expireDate: Long,
    val productProperty: ProductProperty
)
