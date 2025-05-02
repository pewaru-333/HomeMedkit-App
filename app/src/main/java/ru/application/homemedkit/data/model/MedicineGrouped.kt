package ru.application.homemedkit.data.model

import ru.application.homemedkit.data.dto.Kit

data class MedicineGrouped(
    val kit: Kit,
    val medicines: List<MedicineList>
)
