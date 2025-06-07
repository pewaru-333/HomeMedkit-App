package ru.application.homemedkit.data.model

data class MedicineGrouped(
    val kit: KitModel,
    val medicines: List<MedicineList>
)
