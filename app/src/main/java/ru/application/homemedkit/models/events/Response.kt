package ru.application.homemedkit.models.events

sealed interface Response {
    data object Duplicate : Response
    data object Loading : Response
    data object IncorrectCode : Response
    data class NetworkError(val code: String? = null) : Response
    data object UnknownError : Response
    data class Success(val id: Long, val duplicate: Boolean = false) : Response
}