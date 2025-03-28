package ru.application.homemedkit.data.model

import androidx.room.Relation
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.helpers.enums.DoseTypes

data class MedicineMain(
    val id: Long,
    val productName: String,
    val nameAlias: String,
    val prodAmount: Double,
    val doseType: DoseTypes,
    val expDate: Long,
    val prodFormNormName: String,
    val structure: String,
    val phKinetics: String,

    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId",
        projection = ["image"],
        entity = Image::class
    )
    val image: List<String>
)