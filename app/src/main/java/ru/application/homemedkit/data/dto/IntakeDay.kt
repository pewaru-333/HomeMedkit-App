package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import java.time.DayOfWeek

@Entity(
    tableName = "intake_days",
    primaryKeys = ["intakeId", "day"],
    foreignKeys = [
        ForeignKey(
            entity = Intake::class,
            parentColumns = ["intakeId"],
            childColumns = ["intakeId"],
            onUpdate = CASCADE,
            onDelete = CASCADE
        )
    ]
)
data class IntakeDay(
    val intakeId: Long,
    val day: DayOfWeek
)
