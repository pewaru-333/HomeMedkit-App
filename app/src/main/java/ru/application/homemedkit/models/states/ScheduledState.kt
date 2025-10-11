package ru.application.homemedkit.models.states

import ru.application.homemedkit.R
import ru.application.homemedkit.data.model.IntakeModel
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ResourceText

data class ScheduledState(
    override val id: Long = 0L,
    override val alarmId: Long = 0L,
    override val title: String = BLANK,
    override val doseAmount: ResourceText.StringResource = ResourceText.StringResource(R.string.blank),
    override val image: String = BLANK,
    override val time: String = BLANK,
    val date: String = BLANK,
    override val taken: Boolean = false
) : IntakeModel
