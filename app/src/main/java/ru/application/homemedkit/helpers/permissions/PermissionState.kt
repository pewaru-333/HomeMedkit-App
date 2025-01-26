package ru.application.homemedkit.helpers.permissions

interface PermissionState {
    var isGranted: Boolean
    var showRationale: Boolean

    fun launchRequest()
    fun refresh()
    fun openSettings()
}