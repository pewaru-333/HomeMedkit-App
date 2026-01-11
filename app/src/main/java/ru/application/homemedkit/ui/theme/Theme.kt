@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package ru.application.homemedkit.ui.theme

import android.graphics.Color
import android.graphics.Color.argb
import android.os.Build
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.Theme
import ru.application.homemedkit.utils.extensions.getActivity

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val activity = LocalContext.current.getActivity() ?: return

    val darkState by Preferences.theme.collectAsStateWithLifecycle(Theme.SYSTEM)
    val dynamicColor by Preferences.dynamicColors.collectAsStateWithLifecycle(false)

    val darkTheme = when (darkState) {
        Theme.LIGHT -> false
        Theme.DARK, Theme.DARK_AMOLED -> true
        else -> isSystemInDarkTheme()
    }

    val baseColors = when {
        isDynamicColorAvailable() && dynamicColor ->
            if (darkTheme) dynamicDarkColorScheme(activity)
            else dynamicLightColorScheme(activity)

        darkTheme -> darkScheme
        else -> lightScheme
    }

    val colors = if (darkState != Theme.DARK_AMOLED) baseColors else baseColors.copy(
        background = androidx.compose.ui.graphics.Color.Black,
        surface = androidx.compose.ui.graphics.Color.Black,
        surfaceContainer = androidx.compose.ui.graphics.Color.Black,
        surfaceContainerLow = androidx.compose.ui.graphics.Color.Black,
        surfaceContainerLowest = androidx.compose.ui.graphics.Color.Black,
        surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF121212)
    )

    DisposableEffect(darkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkTheme }
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = argb(0xe6, 0xFF, 0xFF, 0xFF),
                darkScrim = argb(0x80, 0x1b, 0x1b, 0x1b),
                detectDarkMode = { darkTheme }
            )
        )

        onDispose { }
    }

    MaterialExpressiveTheme(colorScheme = colors, content = content)
}

fun isDynamicColorAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S