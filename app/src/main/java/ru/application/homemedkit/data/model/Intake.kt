package ru.application.homemedkit.data.model

import ru.application.homemedkit.utils.ResourceText

data class Intake(
    val intakeId: Long,
    val title: String,
    val interval: ResourceText,
    val days: ResourceText,
    val time: String,
    val image: String,
    val active: Boolean
)
