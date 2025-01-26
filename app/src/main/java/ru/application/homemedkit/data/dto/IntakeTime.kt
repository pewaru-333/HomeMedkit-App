package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK

@Entity(
    tableName = "intake_time",
    foreignKeys = [
        ForeignKey(
            entity = Intake::class,
            parentColumns = ["intakeId"],
            childColumns = ["intakeId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IntakeTime(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val intakeId: Long = 0L,
    val time: String = BLANK,
    val amount: Double = 0.0,
    val firstTrigger: Long = 0L
)
