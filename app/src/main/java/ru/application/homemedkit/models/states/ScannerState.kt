package ru.application.homemedkit.models.states

data class ScannerState(
    val code: String? = null,
    val doImageAnalysis: Boolean = true,
    val loading: Boolean = false,
    val incorrectCode: Boolean = false,
    val noNetwork: Boolean = false,
    val error: Boolean = false
)