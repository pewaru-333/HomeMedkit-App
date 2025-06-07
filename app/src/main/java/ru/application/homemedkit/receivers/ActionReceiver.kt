package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ID
import ru.application.homemedkit.utils.TAKEN_ID
import ru.application.homemedkit.utils.TYPE

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = MedicineDatabase.getInstance(context)

        val medicineId = intent.getLongExtra(ID, 0L)
        val takenId = intent.getLongExtra(TAKEN_ID, 0L)
        val amount = intent.getDoubleExtra(BLANK, 0.0)

        NotificationManagerCompat.from(context).cancel(takenId.toInt())
        database.takenDAO().setNotified(takenId)
        if (intent.action == TYPE) {
            database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
            database.medicineDAO().intakeMedicine(medicineId, amount)
        }
    }
}