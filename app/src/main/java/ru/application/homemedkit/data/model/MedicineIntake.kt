package ru.application.homemedkit.data.model

import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.DoseType

data class MedicineIntake(
    val productName: String = BLANK,
    val nameAlias: String = BLANK,
    val prodFormNormName: String = BLANK,
    val expDate: Long = -1L,
    val prodAmount: Double = 0.0,
    val doseType: DoseType = DoseType.MILLIGRAMS
)
