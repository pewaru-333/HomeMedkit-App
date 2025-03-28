package ru.application.homemedkit.data.model

import ru.application.homemedkit.helpers.ResourceText

data class TakenModel(
    override val id: Long,
    override val alarmId: Long,
    override val title: String,
    override val doseAmount: ResourceText.StringResource,
    override val image: String,
    override val time: String,
    override val taken: Boolean
) : IntakeModel
