package ru.application.homemedkit.databaseController

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.PrimaryKey
import ru.application.homemedkit.helpers.BLANK

@Entity(
    tableName = "medicines",
    foreignKeys = [ForeignKey(
        entity = Kit::class,
        parentColumns = arrayOf("kitId"),
        childColumns = arrayOf("kitId"),
        onUpdate = CASCADE,
        onDelete = SET_NULL
    )]
)
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val kitId: Long? = null,
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