package ru.application.homemedkit.models.validation

import ru.application.homemedkit.R.string.text_fill_field
import ru.application.homemedkit.data.model.IntakeAmountTime

object Validation {
    fun checkAmount(list: List<IntakeAmountTime>) = when {
        list.all { it.amount.isNotEmpty() } -> ValidationResult(successful = true)
        else -> ValidationResult(
            successful = false,
            errorMessage = text_fill_field
        )
    }

    fun checkTime(list: List<IntakeAmountTime>) = when {
        list.all { it.time.isNotEmpty() } -> ValidationResult(successful = true)
        else -> ValidationResult(
            successful = false,
            errorMessage = text_fill_field
        )
    }

    fun textNotEmpty(text: String) = if (text.isNotEmpty()) ValidationResult(successful = true)
    else ValidationResult(
        successful = false,
        errorMessage = text_fill_field
    )
}