package ru.application.homemedkit.ui.elements

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import ru.application.homemedkit.R

@Composable
fun NavigationIcon(onNavigate: () -> Unit) = IconButton(onNavigate) { VectorIcon(R.drawable.vector_arrow_back) }

@Composable
fun VectorIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) = Icon(
    imageVector = ImageVector.vectorResource(icon),
    contentDescription = null,
    modifier = modifier,
    tint = tint
)