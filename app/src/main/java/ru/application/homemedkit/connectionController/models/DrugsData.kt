package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import ru.application.homemedkit.helpers.BLANK

data class DrugsData(
    @field:Json val prodDescLabel: String = BLANK,
    @field:Json val foiv: Foiv = Foiv(),
    @field:Json val expireDate: Long = -1L,
    @field:Json val vidalData: VidalData = VidalData()
)
