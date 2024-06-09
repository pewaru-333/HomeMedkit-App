package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.application.homemedkit.helpers.BLANK

@JsonClass(generateAdapter = true)
data class Foiv(
    @Json val prodFormNormName: String = BLANK,
    @Json val prodDNormName: String? = BLANK,
    @Json val prodPack1Size: String? = BLANK
)
