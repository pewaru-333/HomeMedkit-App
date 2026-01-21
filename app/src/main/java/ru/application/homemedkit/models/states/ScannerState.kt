package ru.application.homemedkit.models.states

sealed interface ScannerState {
    data object Default : ScannerState
    data object Idle : ScannerState
    data object Loading : ScannerState

    data class ShowDialog(val code: String? = null) : ScannerState
}