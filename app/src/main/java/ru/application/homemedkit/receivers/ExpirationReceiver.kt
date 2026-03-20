package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.utils.CHANNEL_ID_EXP
import ru.application.homemedkit.utils.extensions.goAsync
import ru.application.homemedkit.utils.extensions.safeNotify

class ExpirationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val inMonth = System.currentTimeMillis() + 30 * 86400000L // millis in day

        MedicineDatabase.getInstance(context).medicineDAO().getExpiredSoon(inMonth).forEach {
            NotificationManagerCompat.from(context).safeNotify(
                context = context,
                code = it.id.toInt(),
                notification = NotificationCompat.Builder(context, CHANNEL_ID_EXP)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentText(context.getString(R.string.text_expire_soon, it.nameAlias.ifEmpty(it::productName)))
                    .setContentTitle(context.getString(R.string.text_attention))
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
        }

        AlarmSetter.getInstance(context).checkExpiration(true)
    }
}

