package ru.application.homemedkit.ui.elements

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import ru.application.homemedkit.R

@Composable
fun Image(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) = Image(
    painter = painterResource(image),
    contentDescription = null,
    modifier = modifier,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter
)

@Composable
fun MedicineImage(image: Any?, modifier: Modifier = Modifier, editable: Boolean = false) =
    AsyncImage(
        model = image,
        modifier = modifier,
        contentDescription = null,
        error = painterResource(R.drawable.vector_type_unknown),
        alpha = if (editable) 0.4f else 1f
    )