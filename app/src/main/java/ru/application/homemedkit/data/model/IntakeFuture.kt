package ru.application.homemedkit.data.model

import ru.application.homemedkit.data.dto.Alarm

data class IntakeFuture(
    override val date: Long = 0L,
    override val intakes: List<Alarm> = emptyList()
) : IntakeListScheme<Alarm>
