package ru.application.homemedkit.data.model

import ru.application.homemedkit.utils.ResourceText

interface IntakeModel {
    val id: Long
    val alarmId: Long
    val title: String
    val doseAmount: ResourceText.StringResource
    val image: String
    val time: String
    val taken: Boolean
}