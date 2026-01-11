package ru.application.homemedkit.models.events

sealed interface SettingsEvent {
    data object ShowKits : SettingsEvent
    data object ShowExport : SettingsEvent
    data object ShowFixing : SettingsEvent
    data object ShowClearing : SettingsEvent
    data object ShowPermissions : SettingsEvent
}