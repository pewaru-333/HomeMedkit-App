package ru.application.homemedkit.receivers

import android.app.AlarmManager.INTERVAL_DAY
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_REMINDER
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import ru.application.homemedkit.MainActivity
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.text_attention
import ru.application.homemedkit.R.string.text_expire_soon
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.helpers.CHANNEL_ID_EXP
import ru.application.homemedkit.helpers.extensions.safeNotify

class ExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicines = MedicineDatabase.getInstance(context).medicineDAO().getAll()

        medicines.forEach {
            if (it.expDate < System.currentTimeMillis() + 30 * INTERVAL_DAY && it.prodAmount > 0) {
                NotificationManagerCompat.from(context).safeNotify(
                    context,
                    it.id.toInt(),
                    Builder(context, CHANNEL_ID_EXP)
                        .setAutoCancel(true)
                        .setCategory(CATEGORY_REMINDER)
                        .setContentIntent(TaskStackBuilder.create(context).run {
                            addNextIntentWithParentStack(
                                Intent(context, MainActivity::class.java).apply {
                                    action = Intent.ACTION_VIEW
                                    data = "app://medicines/${it.id}".toUri()
                                }
                            )
                            getPendingIntent(it.id.toInt(), FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
                        })
                        .setContentText(
                            context.getString(text_expire_soon, it.nameAlias.ifEmpty(it::productName))
                        )
                        .setContentTitle(context.getString(text_attention))
                        .setSmallIcon(R.drawable.ic_launcher_notification)
                        .setVisibility(VISIBILITY_PUBLIC)
                        .build()
                )
                AlarmSetter(context).checkExpiration(true)
            }
        }
    }
}

