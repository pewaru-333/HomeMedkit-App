@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TextDate(date: String) = Text(
    text = date,
    style = MaterialTheme.typography.titleMediumEmphasized,
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp, vertical = 12.dp)
)