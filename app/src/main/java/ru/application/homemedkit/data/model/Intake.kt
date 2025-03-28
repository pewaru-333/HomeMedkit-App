package ru.application.homemedkit.data.model

import ru.application.homemedkit.helpers.ResourceText

data class Intake(
    val intakeId: Long,
    val title: String,
    val interval: ResourceText,
    val time: String,
    val image: String
)
