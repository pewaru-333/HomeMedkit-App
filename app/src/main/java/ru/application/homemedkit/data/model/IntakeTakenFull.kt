package ru.application.homemedkit.data.model

import androidx.room.Relation
import ru.application.homemedkit.data.dto.Medicine
import ru.application.homemedkit.utils.enums.DoseType

data class IntakeTakenFull(
    val takenId: Long,
    val medicineId: Long,
    val intakeId: Long,
    val alarmId: Long,
    val productName: String,
    val formName: String,
    val amount: Double,
    val doseType: DoseType,
    val image: String,
    val trigger: Long,
    val inFact: Long,
    val taken: Boolean,
    val notified: Boolean,

    @Relation(
        parentColumn = "medicineId",
        entityColumn = "id"
    )
    val medicine: Medicine?
)
