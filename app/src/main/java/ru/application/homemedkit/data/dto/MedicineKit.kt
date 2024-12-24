package ru.application.homemedkit.data.dto

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "medicines_kits",
    primaryKeys = ["medicineId", "kitId"],
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Kit::class,
            parentColumns = ["kitId"],
            childColumns = ["kitId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicineKit(
    val medicineId: Long,
    val kitId: Long
)