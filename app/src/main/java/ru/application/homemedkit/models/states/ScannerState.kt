package ru.application.homemedkit.models.states

import androidx.compose.material3.SnackbarHostState

data class ScannerState(
    val doImageAnalysis: Boolean = true,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)