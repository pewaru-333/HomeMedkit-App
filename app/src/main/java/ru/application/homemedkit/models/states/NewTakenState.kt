package ru.application.homemedkit.models.states

import ru.application.homemedkit.data.model.MedicineMain
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ResourceText

data class NewTakenState(
    val title: String = BLANK,
    val amount: String = BLANK,
    val doseType: ResourceText = ResourceText.StaticString(BLANK),
    val inStock: String = BLANK,
    val date: String = BLANK,
    val time: String = BLANK,
    val medicine: MedicineMain? = null
)
