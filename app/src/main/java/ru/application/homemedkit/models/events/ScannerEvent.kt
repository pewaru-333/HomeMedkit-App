package ru.application.homemedkit.models.events

import androidx.annotation.StringRes
import ru.application.homemedkit.R

sealed interface ScannerEvent {
    data class Navigate(val id: Long, val code: String? = null, val duplicate: Boolean = false) : ScannerEvent

    sealed class ShowSnackbar(@StringRes open val message: Int) : ScannerEvent {
        data object IncorrectCode : ShowSnackbar(R.string.text_error_not_medicine)
        data class UnknownError(override val message: Int = R.string.text_try_again) : ShowSnackbar(message)
    }
}