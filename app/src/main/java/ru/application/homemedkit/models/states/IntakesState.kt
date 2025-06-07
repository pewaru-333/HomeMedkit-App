package ru.application.homemedkit.models.states

import ru.application.homemedkit.utils.BLANK

data class IntakesState(
    val search: String = BLANK,
    val showDialog: Boolean = false,
    val showDialogDate: Boolean = false,
    val showDialogDelete: Boolean = false,
    val showDialogAddTaken: Boolean = false
)