package ru.application.homemedkit.data.model

import androidx.room.Relation
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.IntakeTime

data class IntakeList(
    val intakeId: Long,
    val medicineId: Long,
    val interval: Int,
    val productName: String,
    val nameAlias: String,

    @Relation(
        entity = Image::class,
        parentColumn = "medicineId",
        entityColumn = "medicineId",
        projection = ["image"]
    )
    val image: List<String>,

    @Relation(
        entity = IntakeTime::class,
        parentColumn = "intakeId",
        entityColumn = "intakeId",
        projection = ["time"]
    )
    val time: List<String>
)
