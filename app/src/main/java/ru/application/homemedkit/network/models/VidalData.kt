package ru.application.homemedkit.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.application.homemedkit.helpers.BLANK

@JsonClass(generateAdapter = true)
data class VidalData(
    @Json val phKinetics: String? = BLANK,
    @Json val images: List<String>? = emptyList()
)