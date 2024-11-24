package ru.application.homemedkit.models.events

sealed interface Response {
    data object Default : Response
    data object Loading : Response
    data object IncorrectCode : Response
    data object Error : Response
    data class Success(val id: Long) : Response
    data class Duplicate(val id: Long) : Response
    data class NoNetwork(val cis: String) : Response
}