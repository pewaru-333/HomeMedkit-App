package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.application.homemedkit.helpers.BLANK

@JsonClass(generateAdapter = true)
data class DrugsData(
    @Json val prodDescLabel: String = BLANK,
    @Json val foiv: Foiv = Foiv(),
    @Json val expireDate: Long = -1L,
    @Json val vidalData: VidalData = VidalData()
)
