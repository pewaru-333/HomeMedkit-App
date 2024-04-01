package ru.application.homemedkit.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import ru.application.homemedkit.databaseController.MedicineDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            val database = MedicineDatabase.getInstance(context)
            val alarms = database.alarmDAO().getAll()
            val alarmSetter = AlarmSetter(context)

            if (alarms.isNotEmpty()) alarms.forEach { alarmSetter.setAlarm(it.alarmId) }
        }
    }
}