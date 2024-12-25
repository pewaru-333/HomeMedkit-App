package ru.application.homemedkit.data.model

import ru.application.homemedkit.data.dto.Alarm

data class IntakeFuture(
    val date: Long = 0L,
    val intakes: List<Alarm> = emptyList()
)
