package ru.application.homemedkit.data.model

import androidx.room.Junction
import androidx.room.Relation
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.dto.MedicineKit
import ru.application.homemedkit.utils.enums.DoseType

data class MedicineFull(
    val id: Long,
    val cis: String,
    val productName: String,
    val nameAlias: String,
    val expDate: Long,
    val packageOpenedDate: Long,
    val prodFormNormName: String,
    val structure: String,
    val prodDNormName: String,
    val prodAmount: Double,
    val doseType: DoseType,
    val phKinetics: String,
    val recommendations: String,
    val storageConditions: String,
    val comment: String,
    val scanned: Boolean,
    val verified: Boolean,

    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val images: List<Image>,

    @Relation(
        entity = Kit::class,
        parentColumn = "id",
        entityColumn = "kitId",
        associateBy = Junction(
            value = MedicineKit::class,
            parentColumn = "medicineId",
            entityColumn = "kitId"
        )
    )
    val kits: List<Kit>
)
