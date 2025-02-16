package ru.application.homemedkit.models.events

sealed interface Response {
    data class Success(val id: Long, val duplicate: Boolean = false) : Response
}