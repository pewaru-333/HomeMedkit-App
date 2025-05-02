package ru.application.homemedkit.models.states

import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.enums.IntakeTab

data class IntakesState(
    val search: String = BLANK,
    val tab: IntakeTab = IntakeTab.LIST,
    val showDialog: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogDelete: Boolean = false
)