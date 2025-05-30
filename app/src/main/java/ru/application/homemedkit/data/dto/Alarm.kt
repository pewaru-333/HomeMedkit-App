package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
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
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Long = 0L,
    val intakeId: Long = 0L,
    val trigger: Long = 0L,
    val amount: Double = 0.0,
    val preAlarm: Boolean = false
)