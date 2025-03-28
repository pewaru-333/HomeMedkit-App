package ru.application.homemedkit.data.dto

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.enums.DoseTypes

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val cis: String = BLANK,
    val productName: String = BLANK,
    val nameAlias: String = BLANK,
    val expDate: Long = -1L,
    @ColumnInfo(defaultValue = "-1")
    val packageOpenedDate: Long = -1L,
    val prodFormNormName: String = BLANK,
    val structure: String = BLANK,
    val prodDNormName: String = BLANK,
    val prodAmount: Double = -1.0,
    val doseType: DoseTypes = DoseTypes.UNKNOWN,
    val phKinetics: String = BLANK,
    val recommendations: String = BLANK,
    val storageConditions: String = BLANK,
    val comment: String = BLANK,
    @Embedded
    val technical: Technical = Technical()
)