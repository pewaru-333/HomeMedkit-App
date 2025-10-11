package ru.application.homemedkit.models.states

import ru.application.homemedkit.utils.BLANK

data class IntakesState(
    val search: String = BLANK,
    val dialogState: IntakesDialogState? = null
)