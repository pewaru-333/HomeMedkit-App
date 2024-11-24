package ru.application.homemedkit.network.models

import kotlinx.serialization.Serializable
import ru.application.homemedkit.network.models.bio.BioData
import ru.application.homemedkit.network.models.medicine.DrugsData

@Serializable
data class MainModel(
    val codeFounded: Boolean,
    val checkResult: Boolean,
    val category: String?,
    val code: String,
    val drugsData: DrugsData?,
    val bioData: BioData?
)
