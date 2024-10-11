package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
    foreignKeys = [ForeignKey(
        entity = Intake::class,
        parentColumns = arrayOf("intakeId"),
        childColumns = arrayOf("intakeId"),
        onUpdate = CASCADE,
        onDelete = CASCADE
    )]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val alarmId: Long = 0L,
    val intakeId: Long = 0L,
    val trigger: Long = 0L,
    val preAlarm: Boolean = false
)