package ru.application.homemedkit.models.validation

import ru.application.homemedkit.R

object Validation {
    fun textNotEmpty(title: String) = if (title.isNotBlank()) ValidationResult(successful = true)
    else ValidationResult(
        successful = false,
        errorMessage = R.string.text_fill_field
    )

    fun listNotEmpty(list: List<String>) = if(list.all(String::isNotBlank)) ValidationResult(successful = true)
    else ValidationResult(
        successful = false,
        errorMessage = R.string.text_fill_field
    )
 }