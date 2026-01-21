package ru.application.homemedkit.ui.elements

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class CustomSnackbar(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val isError: Boolean = false
) : SnackbarVisuals {
    val containerColor: Color
        @Composable get() = if (isError) MaterialTheme.colorScheme.errorContainer
        else SnackbarDefaults.color

    val contentColor: Color
        @Composable get() = if (isError) MaterialTheme.colorScheme.onErrorContainer
        else SnackbarDefaults.contentColor
}