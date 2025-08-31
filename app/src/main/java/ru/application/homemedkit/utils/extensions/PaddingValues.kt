package ru.application.homemedkit.utils.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

fun PaddingValues.medicine() = PaddingValues(
    start = 16.dp,
    top = calculateTopPadding().plus(16.dp),
    end = 16.dp,
    bottom = 8.dp
)

fun PaddingValues.intake() = PaddingValues(
    start = 8.dp,
    top = calculateTopPadding().plus(16.dp),
    end = 8.dp,
    bottom = 8.dp
)