package ru.application.homemedkit.data.model

import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.enums.DoseTypes

data class MedicineIntake(
    val productName: String = BLANK,
    val nameAlias: String = BLANK,
    val prodFormNormName: String = BLANK,
    val expDate: Long = -1L,
    val prodAmount: Double = 0.0,
    val doseType: DoseTypes = DoseTypes.MILLIGRAMS
)
