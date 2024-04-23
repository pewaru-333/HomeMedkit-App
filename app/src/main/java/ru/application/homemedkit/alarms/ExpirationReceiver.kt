package ru.application.homemedkit.alarms

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.media.RingtoneManager.getRingtone
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MainActivity
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.Preferences
import ru.application.homemedkit.helpers.SOUND_GROUP

class ExpirationReceiver: BroadcastReceiver() {
    private fun expirationNotification(context: Context, medicineId: Long): Notification {
        val database = MedicineDatabase.getInstance(context)
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(ID, medicineId)
        val productName = database.medicineDAO().getProductName(medicineId)

        val pending = getActivity(
            context,
            medicineId.toInt(),
            intent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
            .setSmallIcon(R.drawable.vector_time)
            .setContentTitle(context.getString(R.string.text_attention))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentText(String.format(context.getString(R.string.text_expire_soon), productName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setGroup(SOUND_GROUP)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
          //  .setContentIntent(pending)
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val medicines = MedicineDatabase.getInstance(context).medicineDAO().getAll()
        val check = Preferences(context).getCheckExpDate()

        if (check && medicines.isNotEmpty()) {
            medicines.forEach { medicine ->
                if (medicine.expDate < System.currentTimeMillis() + 30 * AlarmManager.INTERVAL_DAY && medicine.prodAmount > 0) {
                    NotificationManagerCompat.from(context)
                        .notify(medicine.id.toInt(), expirationNotification(context, medicine.id))
                    playSound(context)
                    AlarmSetter(context).checkExpiration()
                }
            }
        }
    }

    private fun playSound(context: Context) = getRingtone(context, getDefaultUri(TYPE_NOTIFICATION)).play()
}