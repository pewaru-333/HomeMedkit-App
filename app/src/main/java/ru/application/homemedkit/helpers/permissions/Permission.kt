package ru.application.homemedkit.helpers.permissions

import android.Manifest.permission.CAMERA
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.Manifest.permission.USE_FULL_SCREEN_INTENT
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import ru.application.homemedkit.helpers.extensions.canScheduleExactAlarms
import ru.application.homemedkit.helpers.extensions.canUseFullScreenIntent
import ru.application.homemedkit.helpers.extensions.isIgnoringBatteryOptimizations

@Stable
class Permission(private val context: Context, private val permission: String) : PermissionState {
    override var showRationale by mutableStateOf(false)
    override var isGranted by mutableStateOf(hasPermission())

    override fun launchRequest() {
        if (permission !in listOf(POST_NOTIFICATIONS, CAMERA)) openSettings()
        else if (showRationale) openSettings()
        else launcher?.launch(permission)
    }

    override fun refresh() {
        isGranted = hasPermission()
    }

    override fun openSettings() {
        val action = when (permission) {
            SCHEDULE_EXACT_ALARM -> ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            POST_NOTIFICATIONS -> ACTION_APP_NOTIFICATION_SETTINGS
            USE_FULL_SCREEN_INTENT -> ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
            REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            else -> ACTION_APPLICATION_DETAILS_SETTINGS
        }

        context.startActivity(
            Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (action == ACTION_APP_NOTIFICATION_SETTINGS) putExtra(EXTRA_APP_PACKAGE, context.packageName)
                else data = Uri.fromParts("package", context.packageName, null)
            }
        )
    }

    internal var launcher: ActivityResultLauncher<String>? = null

    private fun hasPermission(): Boolean {
        val granted = when (permission) {
            SCHEDULE_EXACT_ALARM -> context.canScheduleExactAlarms()
            USE_FULL_SCREEN_INTENT -> context.canUseFullScreenIntent()
            REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> context.isIgnoringBatteryOptimizations()
            else -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        showRationale = !granted && ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)

        return granted
    }
}

@Composable
fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    val permissionState = remember(permission) { Permission(context, permission) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionState.refresh()
    }

    LifecycleResumeEffect(permission, launcher) {
        if (!permissionState.isGranted) permissionState.refresh()
        if (permissionState.launcher == null) permissionState.launcher = launcher

        onPauseOrDispose { permissionState.launcher = null }
    }

    return permissionState
}