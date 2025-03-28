package ru.application.homemedkit.models.events

import android.content.Context

sealed interface TakenEvent {
    data class SaveTaken(val context: Context) : TakenEvent

    data class SetSelection(val index: Int) : TakenEvent
    data object SetFactTime : TakenEvent

    data class ShowTimePicker(val flag: Boolean) : TakenEvent
    data object HideDialog : TakenEvent
}