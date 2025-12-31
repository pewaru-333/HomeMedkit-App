package ru.application.homemedkit.data.model

import androidx.compose.runtime.Stable
import ru.application.homemedkit.utils.ResourceText

@Stable
data class MedicineList(
    val id: Long,
    val title: String,
    val prodAmountDoseType: ResourceText,
    val expDateS: String,
    val formName: String,
    val image: String,
    val inStock: Boolean,
    val isExpired: Boolean
)
