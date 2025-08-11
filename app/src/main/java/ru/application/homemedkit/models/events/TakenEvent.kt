package ru.application.homemedkit.models.events

import androidx.core.app.NotificationManagerCompat

sealed interface TakenEvent {
    data class SaveTaken(val manager: NotificationManagerCompat) : TakenEvent

    data class SetSelection(val index: Int) : TakenEvent
    data object SetFactTime : TakenEvent

    data class ShowTimePicker(val flag: Boolean) : TakenEvent
    data object HideDialog : TakenEvent
}