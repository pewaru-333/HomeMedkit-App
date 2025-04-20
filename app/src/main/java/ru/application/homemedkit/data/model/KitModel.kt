package ru.application.homemedkit.data.model

data class KitModel(
    val kitId: Long,
    val medicineId: Long?,
    val title: String,
    val position: Long
)
