package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import ru.application.homemedkit.data.MedicineDatabase

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_BOOT_COMPLETED) {
            val takenDAO = MedicineDatabase.getInstance(context).takenDAO()

            AlarmSetter(context).resetAll()
            takenDAO.getAll().forEach { takenDAO.setNotified(it.takenId) }
        }
    }
}