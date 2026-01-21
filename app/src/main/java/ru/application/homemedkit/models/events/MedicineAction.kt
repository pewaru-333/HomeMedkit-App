package ru.application.homemedkit.models.events

import androidx.annotation.StringRes
import ru.application.homemedkit.R

sealed interface MedicineAction {
    data object OnDelete : MedicineAction

    sealed class ShowSnackbar(@StringRes open val message: Int) : MedicineAction {
        data object OnMakeDuplicate : ShowSnackbar(R.string.text_success)
        data object OnReceiveDuplicate : ShowSnackbar(R.string.text_duplicate)
        data class OnShowError(@StringRes override val message: Int = R.string.text_error) : ShowSnackbar(message)
    }
}