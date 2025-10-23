package ru.application.homemedkit.network.models.medicine

import kotlinx.serialization.Serializable

@Serializable
data class DrugsData(
    val prodDescLabel: String,
    val foiv: Foiv,
    val expireDate: Long,
    val vidalData: VidalData?
) {

    @Serializable
    data class Foiv(
        val prodFormNormName: String,
        val prodDNormName: String?,
        val prodPack12: String?,
        val prodPack1Size: String?
    )

    @Serializable
    data class VidalData(
        val phKinetics: String?,
        val images: List<String>?
    )
}
