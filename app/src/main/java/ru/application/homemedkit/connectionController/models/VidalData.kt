package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import ru.application.homemedkit.helpers.BLANK

data class VidalData(
    @field:Json val phKinetics: String? = BLANK,
    @field:Json val images: List<String>? = emptyList()
)