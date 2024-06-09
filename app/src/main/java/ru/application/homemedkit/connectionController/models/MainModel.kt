package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.application.homemedkit.helpers.BLANK

@JsonClass(generateAdapter = true)
data class MainModel(
    @Json val codeFounded: Boolean,
    @Json val checkResult: Boolean,
    @Json val category: String? = BLANK,
    @Json val cis: String = BLANK,
    @Json val drugsData: DrugsData = DrugsData()
)
