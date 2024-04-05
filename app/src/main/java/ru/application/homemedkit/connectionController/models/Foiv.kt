package ru.application.homemedkit.connectionController.models

import com.squareup.moshi.Json
import ru.application.homemedkit.helpers.BLANK

data class Foiv(
    @field:Json val prodFormNormName: String = BLANK,
    @field:Json val prodDNormName: String? = BLANK,
    @field:Json val prodPack1Size: String? = BLANK
)
