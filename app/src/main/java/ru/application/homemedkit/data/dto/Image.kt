package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ru.application.homemedkit.utils.BLANK

@Entity(
    tableName = "images",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Image(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val medicineId: Long = 0L,
    val image: String = BLANK
)
