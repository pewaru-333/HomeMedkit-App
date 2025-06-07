package ru.application.homemedkit.models.events

import androidx.annotation.StringRes
import ru.application.homemedkit.R
import ru.application.homemedkit.network.models.MainModel

sealed interface Response {
    data object Initial : Response
    data object Loading : Response
    data object Duplicate : Response
    data class Success(val model: MainModel) : Response
    data class Navigate(val id: Long, val duplicate: Boolean = false) : Response

    sealed class Error(@StringRes val message: Int) : Response {
        data object IncorrectCode : Error(R.string.text_error_not_medicine)
        data object CodeNotFound : Error(R.string.text_code_not_found)
        data object UnknownError : Error(R.string.text_try_again)
        data class NetworkError(val code: String? = null) : Error(R.string.text_try_again)
    }
}