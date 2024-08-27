package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_LOCALE_CHANGED

class LocaleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_LOCALE_CHANGED) createNotificationChannel(context)
    }
}