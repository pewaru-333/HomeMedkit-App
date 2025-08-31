package ru.application.homemedkit.models.states

data class SettingsState(
    val showKits: Boolean = false,
    val showExport: Boolean = false,
    val showFixing: Boolean = false,
    val showClearing: Boolean = false,
    val showPermissions: Boolean = false
)
