package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_REMINDER
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import ru.application.homemedkit.R
import ru.application.homemedkit.R.string.text_intake_prealarm_text
import ru.application.homemedkit.R.string.text_intake_prealarm_title
import ru.application.homemedkit.data.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.utils.ALARM_ID
import ru.application.homemedkit.utils.BLANK
import ru.application.homemedkit.utils.CHANNEL_ID_PRE
import ru.application.homemedkit.utils.FORMAT_H_MM
import ru.application.homemedkit.utils.decimalFormat
import ru.application.homemedkit.utils.extensions.goAsync
import ru.application.homemedkit.utils.extensions.safeNotify
import ru.application.homemedkit.utils.getDateTime

class PreAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)

        val alarm = database.alarmDAO().getById(alarmId) ?: return
        val intake = database.intakeDAO().getById(alarm.intakeId) ?: return
        val medicine = database.medicineDAO().getById(intake.medicineId) ?: return
        val image = database.medicineDAO().getMedicineImages(medicine.id).firstOrNull() ?: BLANK

        goAsync(Dispatchers.IO) {
            database.alarmDAO().delete(alarm)

            val takenId = database.takenDAO().insert(
                IntakeTaken(
                    medicineId = medicine.id,
                    intakeId = alarm.intakeId,
                    alarmId = alarmId,
                    productName = medicine.nameAlias.ifEmpty(medicine::productName),
                    formName = medicine.prodFormNormName,
                    amount = alarm.amount,
                    doseType = medicine.doseType,
                    image = image,
                    trigger = alarm.trigger
                )
            )

            AlarmSetter(context).setAlarm(takenId, alarm.trigger)
        }

        if (alarm.preAlarm) with(NotificationManagerCompat.from(context)) {
            safeNotify(
                context,
                alarmId.toInt(),
                Builder(context, CHANNEL_ID_PRE)
                    .setCategory(CATEGORY_REMINDER)
                    .setContentTitle(context.getString(text_intake_prealarm_title))
                    .setSilent(true)
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setStyle(
                        BigTextStyle().bigText(
                            context.getString(
                                text_intake_prealarm_text,
                                medicine.nameAlias.ifEmpty(medicine::productName),
                                decimalFormat(alarm.amount),
                                context.getString(medicine.doseType.title),
                                getDateTime(alarm.trigger).format(FORMAT_H_MM)
                            )
                        )
                    )
                    .setTimeoutAfter(1800000L)
                    .setVisibility(VISIBILITY_PUBLIC)
                    .build()
            )
        }
    }
}