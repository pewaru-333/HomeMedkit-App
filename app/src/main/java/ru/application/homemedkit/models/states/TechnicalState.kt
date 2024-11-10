package ru.application.homemedkit.models.states

data class TechnicalState(
    val scanned: Boolean = false,
    val verified: Boolean = false
)