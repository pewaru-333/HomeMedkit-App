package ru.application.homemedkit.receivers

import android.annotation.SuppressLint
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import ru.application.homemedkit.MainActivity
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.text_attention
import ru.application.homemedkit.R.string.text_expire_soon
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.helpers.CHANNEL_ID_EXP

class ExpirationReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val dao = MedicineDatabase.getInstance(context).medicineDAO()
        val medicines = runBlocking { dao.getFlow().firstOrNull() }

        if (!medicines.isNullOrEmpty()) medicines.forEach { (id, _, _, _, expDate, _, _, _, prodAmount) ->
            if (expDate < System.currentTimeMillis() + 30 * INTERVAL_DAY && prodAmount > 0) {
                NotificationManagerCompat.from(context).notify(
                    id.toInt(),
                    Builder(context, CHANNEL_ID_EXP)
                        .setAutoCancel(true)
                        .setCategory(CATEGORY_REMINDER)
                        .setContentIntent(TaskStackBuilder.create(context).run {
                            addNextIntentWithParentStack(Intent(context, MainActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                                data = "app://medicines/$id".toUri()
                            })
                            getPendingIntent(id.toInt(), FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
                        })
                        .setContentText(context.getString(text_expire_soon, dao.getProductName(id)))
                        .setContentTitle(context.getString(text_attention))
                        .setSmallIcon(vector_time)
                        .setVisibility(VISIBILITY_PUBLIC)
                        .build()
                )
                AlarmSetter(context).checkExpiration(true)
            }
        }
    }
}

