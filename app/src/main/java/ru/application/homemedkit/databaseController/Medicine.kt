package ru.application.homemedkit.databaseController

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val cis: String = BLANK,
    val productName: String = BLANK,
    val expDate: Long = -1L,
    val prodFormNormName: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: Double = -1.0,
    val doseType: String = BLANK,
    val phKinetics: String = BLANK,
    val comment: String = BLANK,
    val image: String = BLANK,
    @Embedded
    val technical: Technical = Technical()
)