package ru.application.homemedkit.databaseController

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.application.homemedkit.helpers.BLANK
import java.time.LocalTime

@Entity(
    tableName = "intakes",
    foreignKeys = [ForeignKey(
        entity = Medicine::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("medicineId"),
        onUpdate = CASCADE,
        onDelete = CASCADE
    )]
)
data class Intake(
    @PrimaryKey(autoGenerate = true)
    val intakeId: Long = 0L,
    val medicineId: Long = 0L,
    val amount: Double = 0.0,
    val interval: Int = 0,
    val foodType: Int = -1,
    @TypeConverters(Converters::class)
    val time: List<LocalTime> = emptyList(),
    val period: Int = 0,
    val startDate: String = BLANK,
    val finalDate: String = BLANK
)