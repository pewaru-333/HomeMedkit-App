package ru.application.homemedkit.alarms

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MedicineActivity
import ru.application.homemedkit.databaseController.MedicineDatabase
import ru.application.homemedkit.helpers.BOUND
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.SOUND_GROUP
import ru.application.homemedkit.helpers.SettingsHelper
import java.util.Random

class ExpirationReceiver: BroadcastReceiver() {

    private val MONTH = 2419200000L

    private fun expirationNotification(context: Context, medicineId: Long): Notification {
        val database = MedicineDatabase.getInstance(context)
        val productName = database.medicineDAO().getProductName(medicineId)
        val code = Random().nextInt(BOUND)
        val intent = Intent(context, MedicineActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(ID, medicineId)
        val pending = PendingIntent.getActivity(
            context, code, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(
            context,
            context.getString(R.string.notification_channel_name)
        )
            .setSmallIcon(R.drawable.vector_time)
            .setContentTitle(context.getString(R.string.text_attention))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentText(
                String.format(
                    context.getString(R.string.text_expire_soon),
                    productName
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setGroup(SOUND_GROUP)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setContentIntent(pending)
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val database = MedicineDatabase.getInstance(context)
        val medicines = database.medicineDAO().getAll()
        val alarmSetter = AlarmSetter(context)

        var notification: Notification
        var compat: NotificationManagerCompat

        val flag = SettingsHelper(context).checkExpirationDate()

        if (flag && medicines.isNotEmpty()) {
            medicines.forEach { medicine ->
                if (medicine.expDate < System.currentTimeMillis() + MONTH && medicine.prodAmount > 0) {
                    notification = expirationNotification(context, medicine.id)
                    compat = NotificationManagerCompat.from(context)
                    compat.notify(Random().nextInt(BOUND), notification)
                    playSound(context)
                    alarmSetter.checkExpiration()
                }
            }
        }
    }

    private fun playSound(context: Context) {
        RingtoneManager.getRingtone(
            context,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ).play()
    }
}