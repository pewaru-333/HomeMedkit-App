package ru.application.homemedkit.data.model

import ru.application.homemedkit.utils.ResourceText

data class KitModel(
    val id: Long,
    val title: ResourceText,
    val position: Long
)
