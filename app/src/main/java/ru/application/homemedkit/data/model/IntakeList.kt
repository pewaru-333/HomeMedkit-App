package ru.application.homemedkit.data.model

import androidx.room.Relation
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.IntakeDay
import ru.application.homemedkit.data.dto.IntakeTime
import java.time.DayOfWeek

data class IntakeList(
    val intakeId: Long,
    val medicineId: Long, // needed for relation
    val productName: String,
    val nameAlias: String,
    val finalDate: String,

    @Relation(
        entity = Image::class,
        parentColumn = "medicineId",
        entityColumn = "medicineId",
        projection = ["image"]
    )
    val image: List<String>,

    @Relation(
        entity = IntakeDay::class,
        parentColumn = "intakeId",
        entityColumn = "intakeId",
        projection = ["day"]
    )
    val days: List<DayOfWeek>,

    @Relation(
        entity = IntakeTime::class,
        parentColumn = "intakeId",
        entityColumn = "intakeId",
        projection = ["time"]
    )
    val time: List<String>
)
