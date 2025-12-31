@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.elements

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (() -> Unit)
) = IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    interactionSource = interactionSource,
    shapes = IconButtonDefaults.shapes(),
    content = content
)