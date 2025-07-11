package ru.application.homemedkit.data.model

data class MedicineList(
    val id: Long,
    val title: String,
    val prodAmount: String,
    val doseType: Int,
    val expDateS: String,
    val formName: String,
    val image: String,
    val inStock: Boolean,
    val isExpired: Boolean
)
