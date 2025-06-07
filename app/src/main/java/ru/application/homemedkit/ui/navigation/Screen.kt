package ru.application.homemedkit.ui.navigation

import kotlinx.serialization.Serializable
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.enums.IntakeTab

sealed interface Screen {
    @Serializable
    object Medicines : Screen

    @Serializable
    data class Intakes(val tab: IntakeTab = IntakeTab.LIST) : Screen

    @Serializable
    object Settings : Screen

    @Serializable
    object Scanner : Screen

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

    // ===== Settings items ===== //
    @Serializable
    object KitsManager : Screen

    @Serializable
    object PermissionsScreen : Screen
}