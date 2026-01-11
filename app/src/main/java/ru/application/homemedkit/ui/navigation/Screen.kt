package ru.application.homemedkit.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.application.homemedkit.utils.BLANK

@Serializable
sealed interface Screen : NavKey {
    sealed interface NavDeepLink {
        val parent: NavKey
    }

    @Serializable
    data object Medicines : Screen

    @Serializable
    data object Intakes : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class Auth(val code: String? = null) : NavDeepLink, Screen {
        override val parent = Settings
    }

    @Serializable
    data object Scanner : Screen

    @Serializable
    data class Medicine(
        val id: Long = 0L,
        val cis: String = BLANK,
        val duplicate: Boolean = false
    ) : Screen

    @Serializable
    data class Intake(
        val intakeId: Long = 0L,
        val medicineId: Long = 0L
    ) : Screen
}