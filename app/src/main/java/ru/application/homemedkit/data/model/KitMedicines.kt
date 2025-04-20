package ru.application.homemedkit.data.model

import androidx.room.Relation
import ru.application.homemedkit.data.dto.MedicineKit

data class KitMedicines(
    val kitId: Long,
    val title: String,
    val position: Long,

    @Relation(
        entity = MedicineKit::class,
        parentColumn = "kitId",
        entityColumn = "kitId",
        projection = ["medicineId"]
    )
    val medicineIdList: List<Long>
)
