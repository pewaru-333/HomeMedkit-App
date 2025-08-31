package ru.application.homemedkit.ui.elements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun NavigationIcon(onNavigate: () -> Unit) = IconButton(onNavigate) {
    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
}