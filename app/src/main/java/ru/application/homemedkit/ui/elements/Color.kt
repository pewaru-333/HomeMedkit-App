package ru.application.homemedkit.ui.elements

import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TextFieldListItemColors: TextFieldColors
    @Composable get() = TextFieldDefaults.colors().copy(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        focusedContainerColor = ListItemDefaults.containerColor,
        unfocusedContainerColor = ListItemDefaults.containerColor,
        errorContainerColor = MaterialTheme.colorScheme.errorContainer
    )
