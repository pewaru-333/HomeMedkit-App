package ru.application.homemedkit.receivers

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
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_ALARM
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.MainActivity
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.helpers.CHANNEL_ID
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.SOUND_GROUP

class ExpirationReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val medicines = MedicineDatabase.getInstance(context).medicineDAO().getAll()

        createNotificationChannel(context)

        if (medicines.isNotEmpty()) medicines.forEach { medicine ->
            if (medicine.expDate < System.currentTimeMillis() + 30 * AlarmManager.INTERVAL_DAY && medicine.prodAmount > 0) {
                NotificationManagerCompat.from(context)
                    .notify(medicine.id.toInt(), expirationNotification(context, medicine.id))
                getRingtone(context, getDefaultUri(TYPE_NOTIFICATION)).play()
                AlarmSetter(context).checkExpiration(true)
            }
        }
    }
}

private fun expirationNotification(context: Context, medicineId: Long): Notification {
    val productName = MedicineDatabase.getInstance(context).medicineDAO().getProductName(medicineId)

    val pending = getActivity(
        context,
        medicineId.toInt(),
        Intent(context, MainActivity::class.java).apply {
            putExtra(ID, medicineId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )

    return Builder(context, CHANNEL_ID)
        .setAutoCancel(true)
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(pending)
        .setContentText(String.format(context.getString(R.string.text_expire_soon), productName))
        .setContentTitle(context.getString(R.string.text_attention))
        .setDefaults(Notification.DEFAULT_ALL)
        .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        .setGroup(SOUND_GROUP)
        .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
        .setSmallIcon(R.drawable.vector_time)
        .build()
}