package ru.application.homemedkit.data.model

import ru.application.homemedkit.helpers.enums.DoseTypes

data class Schedule(
    val alarmId: Long,
    val productName: String,
    val nameAlias: String,
    val prodFormNormName: String,
    val doseType: DoseTypes,
    val amount: Double,
    val image: String,
    val trigger: Long
)
