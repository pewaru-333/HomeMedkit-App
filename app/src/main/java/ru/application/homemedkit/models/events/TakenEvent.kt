package ru.application.homemedkit.models.events

import androidx.core.app.NotificationManagerCompat

sealed interface TakenEvent {
    data class Save(val manager: NotificationManagerCompat) : TakenEvent

    data object Delete : TakenEvent

    data class SetSelection(val index: Int) : TakenEvent

    data class ShowTimePicker(val flag: Boolean) : TakenEvent
    data object SetFactTime : TakenEvent
}