package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.utils.ALARM_ID
import ru.application.homemedkit.utils.CHANNEL_ID_PRE
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.extensions.goAsync
import ru.application.homemedkit.utils.extensions.safeNotify

class PreAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        val database = MedicineDatabase.getInstance(context)

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)

        val alarm = database.alarmDAO().getById(alarmId) ?: return@goAsync
        val intake = database.intakeDAO().getById(alarm.intakeId) ?: return@goAsync
        val medicine = database.medicineDAO().getById(intake.medicineId) ?: return@goAsync
        val image = database.medicineDAO().getMedicineImage(medicine.id).orEmpty()

        database.alarmDAO().delete(alarm)

        val takenId = database.takenDAO().insert(
            item = IntakeTaken(
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

        AlarmSetter.getInstance(context).setAlarm(takenId, alarm.trigger)

        if (alarm.preAlarm) {
            NotificationManagerCompat.from(context).safeNotify(
                context = context,
                code = alarmId.toInt(),
                notification = NotificationCompat.Builder(context, CHANNEL_ID_PRE)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setContentTitle(context.getString(R.string.text_intake_prealarm_title))
                    .setSilent(true)
                    .setSmallIcon(R.drawable.ic_launcher_notification)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(
                            context.getString(
                                R.string.text_intake_prealarm_text,
                                medicine.nameAlias.ifEmpty(medicine::productName),
                                Formatter.decimalFormat(alarm.amount),
                                context.getString(medicine.doseType.title),
                                Formatter.timeFormat(alarm.trigger)
                            )
                        )
                    )
                    .setTimeoutAfter(1800000L)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()
            )
        }
    }
}