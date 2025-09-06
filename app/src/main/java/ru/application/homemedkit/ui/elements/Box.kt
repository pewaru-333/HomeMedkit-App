package ru.application.homemedkit.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun BoxWithEmptyListText(@StringRes text: Int, modifier: Modifier = Modifier) =
    Box(modifier, Alignment.Center) {
        Text(
            text = stringResource(text),
            textAlign = TextAlign.Center
        )
    }

@Composable
fun BoxLoading(modifier: Modifier = Modifier) = Box(
    content = { CircularProgressIndicator() },
    contentAlignment = Alignment.Center,
    modifier = modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.45f))
)