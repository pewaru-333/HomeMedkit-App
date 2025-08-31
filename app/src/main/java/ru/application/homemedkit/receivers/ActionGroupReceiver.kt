package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.ID
import ru.application.homemedkit.utils.IS_ENOUGH_IN_STOCK
import ru.application.homemedkit.utils.TAKEN_ID
import ru.application.homemedkit.utils.TYPE
import ru.application.homemedkit.utils.extensions.goAsync

class ActionGroupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val database = MedicineDatabase.getInstance(context)
        val manager = NotificationManagerCompat.from(context)

        manager.cancel(Int.MAX_VALUE)
        manager.activeNotifications
            .filter { it.packageName == context.packageName }
            .filter { it.notification.extras.containsKey(IS_ENOUGH_IN_STOCK) }
            .forEach { item ->
                val medicineId = item.notification.extras.getLong(ID)
                val takenId = item.notification.extras.getLong(TAKEN_ID)
                val amount = item.notification.extras.getDouble(BLANK)

                manager.cancel(takenId.toInt())
                database.takenDAO().setNotified(takenId)
                if (intent.action == TYPE) {
                    database.takenDAO().setTaken(takenId, true, System.currentTimeMillis())
                    database.medicineDAO().intakeMedicine(medicineId, amount)
                }
            }
    }
}