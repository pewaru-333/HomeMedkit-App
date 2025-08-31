package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.utils.extensions.goAsync

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            AlarmSetter.getInstance(context).resetAll()
            MedicineDatabase.getInstance(context).takenDAO().setNotified()
        }
    }
}