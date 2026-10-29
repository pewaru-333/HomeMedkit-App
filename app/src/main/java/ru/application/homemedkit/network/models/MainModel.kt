package ru.application.homemedkit.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MainModel(
    val codeFounded: Boolean,
    val category: String? = null,
    val code: String,
    val productName: String? = null,
    val expireDate: Long? = null,
    val screen: ScreenData? = null,
    val catalogData: List<CatalogProduct>? = null
) {
    val imageUrls: List<String>
        get() {
            val cardImages = screen?.items
                ?.firstOrNull { it.itemType == "group_card" }
                ?.images

            if (!cardImages.isNullOrEmpty()) return cardImages

            val pharmacyImage = screen?.items
                ?.firstOrNull { it.itemType == "pharmacy_search" }
                ?.pharmacyData?.image

            return pharmacyImage?.let { listOf(it) } ?: emptyList()
        }

    val attributes: Map<String, String>
        get() = buildMap {
            catalogData?.firstOrNull()?.attributes?.forEach {
                it.label?.let { label -> put(label, it.value.orEmpty()) }
            }
            screen?.items?.forEach { item ->
                item.attrList?.forEach {
                    it.label?.let { label -> put(label, it.value.orEmpty()) }
                }
            }
        }

    val pharmacyInfo: PharmacyData?
        get() = screen?.items?.firstOrNull { it.itemType == "pharmacy_search" }?.pharmacyData

    val isValidCategory: Boolean
        get() = category == "drugs" || category == "bio" || category == "antiseptic"
}

@Serializable
data class CatalogProduct(
    @SerialName("good_name") val name: String? = null,
    @SerialName("good_attrs") val attributes: List<CatalogAttribute>? = null
)

@Serializable
data class CatalogAttribute(
    @SerialName("attr_name") val label: String? = null,
    @SerialName("attr_value") val value: String? = null
)

@Serializable
data class ScreenData(
    val items: List<ScreenItem>? = null
)

@Serializable
data class ScreenItem(
    val itemType: String,
    val images: List<String>? = null,
    val pharmacyData: PharmacyData? = null,
    val attrList: List<AttrItem>? = null
)

@Serializable
data class PharmacyData(
    val title: String? = null,
    val activeSubstance: String? = null,
    val form: String? = null,
    val dosage: String? = null,
    val quantity: String? = null,
    val image: String? = null
)

@Serializable
data class AttrItem(
    val label: String? = null,
    val value: String? = null
)