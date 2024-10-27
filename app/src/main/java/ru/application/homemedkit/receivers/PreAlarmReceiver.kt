package ru.application.homemedkit.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_REMINDER
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.text_intake_prealarm_text
import ru.application.homemedkit.R.string.text_intake_prealarm_title
import ru.application.homemedkit.data.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CHANNEL_ID_PRE
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.getDateTime
import ru.application.homemedkit.helpers.lastAlarm

class PreAlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)
        val setter = AlarmSetter(context)

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)
        val intakeId = database.alarmDAO().getByPK(alarmId).intakeId
        val intake = database.intakeDAO().getById(intakeId)!!
        val trigger = database.alarmDAO().getByPK(alarmId).trigger
        val medicine = database.medicineDAO().getById(intake.medicineId)

        NotificationManagerCompat.from(context).notify(
            alarmId.toInt(),
            Builder(context, CHANNEL_ID_PRE)
                .setCategory(CATEGORY_REMINDER)
                .setContentTitle(context.getString(text_intake_prealarm_title))
                .setSilent(true)
                .setSmallIcon(vector_time)
                .setStyle(
                    BigTextStyle().bigText(
                        context.getString(
                            text_intake_prealarm_text,
                            medicine?.productName ?: BLANK,
                            intake.amount.toString(),
                            medicine?.doseType ?: BLANK,
                            getDateTime(trigger).format(FORMAT_H)
                        )
                    )
                )
                .setTimeoutAfter(1800000L)
                .setVisibility(VISIBILITY_PUBLIC)
                .build()
        )

        if (trigger >= lastAlarm(intake.finalDate, intake.time.last().minusMinutes(30))) setter.removeAlarm(alarmId)
        else setter.resetAlarm(alarmId)
    }
}