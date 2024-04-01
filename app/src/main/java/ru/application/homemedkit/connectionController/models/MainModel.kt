package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import ru.application.homemedkit.helpers.BLANK

data class MainModel(
    @field:Json val codeFounded: Boolean,
    @field:Json val checkResult: Boolean,
    @field:Json val category: String? = BLANK,
    @field:Json val cis: String = BLANK,
    @field:Json val drugsData: DrugsData = DrugsData()
)
