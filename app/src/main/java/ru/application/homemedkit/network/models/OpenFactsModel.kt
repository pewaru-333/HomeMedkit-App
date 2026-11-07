package ru.application.homemedkit.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Product response of Open Food / Beauty / Products Facts (api/v2/product/{barcode}.json)

@Serializable
data class OpenFactsResponse(
    val status: Int? = null,
    val product: OpenFactsProduct? = null
)

@Serializable
data class OpenFactsProduct(
    @SerialName("product_name") val name: String? = null,
    val brands: String? = null,
    val quantity: String? = null,
    @SerialName("image_front_url") val image: String? = null,
    @SerialName("ingredients_text") val ingredients: String? = null
)

/**
 * Maps the product to the app's [MainModel], or null if it was not found.
 * Uses the generic "gtin" category, so the user confirms the product before it is saved.
 */
fun OpenFactsResponse.toMainModel(ean: String, sourceName: String): MainModel? {
    if (status != 1) return null
    val product = product ?: return null

    return MainModel.of(
        code = ean,
        category = MainModel.CATEGORY_GTIN,
        productName = product.name ?: product.brands,
        pharmacyData = PharmacyData(
            activeSubstance = product.ingredients,
            // "30 compresse" -> "compresse", used to pick the form icon and dose type
            form = product.quantity?.substringAfter(' ', ""),
            quantity = product.quantity,
            image = product.image
        ),
        // Data is ODbL and photos CC BY-SA: the source must be credited when shown
        attribution = sourceName
    )
}
