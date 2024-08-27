package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK

@Entity(tableName = "intakes_taken")
data class IntakeTaken(
    @PrimaryKey(autoGenerate = true)
    val takenId: Long = 0L,
    val medicineId: Long = 0L,
    val intakeId: Long = 0L,
    val alarmId: Long = 0L,
    val productName: String = BLANK,
    val formName: String = BLANK,
    val amount: Double = 0.0,
    val doseType: String = BLANK,
    val image: String = BLANK,
    val trigger: Long = 0L,
    val taken: Boolean = false,
    val notified: Boolean = false
)