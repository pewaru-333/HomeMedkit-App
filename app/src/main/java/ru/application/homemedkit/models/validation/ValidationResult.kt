package ru.application.homemedkit.models.validation

import androidx.annotation.StringRes

data class ValidationResult(
    val successful: Boolean,
    @StringRes val errorMessage: Int? = null
)
