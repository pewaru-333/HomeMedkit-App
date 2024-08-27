package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import ru.application.homemedkit.data.MedicineDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            val database = MedicineDatabase.getInstance(context)
            val alarms = database.alarmDAO().getAll()
            val taken = database.takenDAO().getAll()
            val setter = AlarmSetter(context)

            if (taken.isNotEmpty()) taken.forEach { database.takenDAO().setNotified(it.takenId) }
            if (alarms.isNotEmpty()) alarms.forEach { setter.setAlarm(it.alarmId) }
        }
    }
}