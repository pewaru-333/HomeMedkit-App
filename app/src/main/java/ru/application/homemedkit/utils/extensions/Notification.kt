package ru.application.homemedkit.utils.extensions

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun NotificationManagerCompat.safeNotify(context: Context, code: Int, notification: Notification) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        notify(code, notification)
    }
}

fun BroadcastReceiver.goAsync(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit
) {
    val parentScope = CoroutineScope(coroutineContext)
    val pendingResult = goAsync()

    parentScope.launch {
        try {
            try {
                coroutineScope { this.block() }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                parentScope.cancel()
            }
        } finally {
            try {
                pendingResult.finish()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}