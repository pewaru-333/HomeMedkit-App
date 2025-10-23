package ru.application.homemedkit.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.application.homemedkit.network.models.bio.BioData
import ru.application.homemedkit.network.models.medicine.DrugsData

@Serializable
data class MainModel(
    val codeFounded: Boolean,
    val checkResult: Boolean,
    val category: String?,
    val code: String,
    val productName: String,
    val catalogData: List<ItemData>?,
    val drugsData: DrugsData?,
    val bioData: BioData?
) {
    @Serializable
    data class ItemData(
        @SerialName("good_img")
        val goodImg: String?,
        @SerialName("good_images")
        val goodImages: List<GoodImage>?
    ) {
        @Serializable
        data class GoodImage(
            @SerialName("photo_url")
            val photoUrl: String
        )
    }
    
    val imageUrls = drugsData?.vidalData?.images
        ?: catalogData?.firstOrNull()?.goodImages?.map { it.photoUrl }
}
