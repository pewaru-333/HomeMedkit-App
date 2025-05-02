package ru.application.homemedkit.data.model

import ru.application.homemedkit.helpers.enums.DoseType

data class Schedule(
    val alarmId: Long,
    val productName: String,
    val nameAlias: String,
    val prodFormNormName: String,
    val doseType: DoseType,
    val amount: Double,
    val image: String,
    val trigger: Long
)
