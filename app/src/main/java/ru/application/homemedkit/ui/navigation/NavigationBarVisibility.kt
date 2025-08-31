package ru.application.homemedkit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import ru.application.homemedkit.utils.BLANK

@Stable
interface NavigationBarVisibility {
    val isVisible: Boolean

    fun show()
    fun hide()
}

private class InitialNavigationBarVisibility : NavigationBarVisibility {
    private val _isVisible = mutableStateOf(true)
    override val isVisible by _isVisible

    override fun show() {
        _isVisible.value = true
    }

    override fun hide() {
        _isVisible.value = false
    }
}

val LocalBarVisibility = staticCompositionLocalOf<NavigationBarVisibility> {
    error(BLANK)
}

@Composable
fun rememberNavigationBarVisibility(): NavigationBarVisibility {
    return remember(::InitialNavigationBarVisibility)
}