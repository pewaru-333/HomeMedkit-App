package ru.application.homemedkit.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
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
import ru.application.homemedkit.helpers.CHANNEL_ID_PRE
import ru.application.homemedkit.helpers.DoseTypes
import ru.application.homemedkit.helpers.FORMAT_H
import ru.application.homemedkit.helpers.decimalFormat
import ru.application.homemedkit.helpers.getDateTime

class PreAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)
        val setter = AlarmSetter(context)

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)
        val alarm = database.alarmDAO().getById(alarmId)
        val intakeId = database.alarmDAO().getById(alarmId).intakeId
        val intake = database.intakeDAO().getById(intakeId)!!
        val preAlarmTrigger = database.alarmDAO().getById(alarmId).trigger
        val alarmTrigger = preAlarmTrigger + 1800000L
        val medicine = database.medicineDAO().getById(intake.medicineId)!!

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            return

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
                            medicine.nameAlias.ifEmpty(medicine::productName),
                            decimalFormat(alarm.amount),
                            context.getString(DoseTypes.getTitle(medicine.doseType)),
                            getDateTime(alarmTrigger).format(FORMAT_H)
                        )
                    )
                )
                .setTimeoutAfter(1800000L)
                .setVisibility(VISIBILITY_PUBLIC)
                .build()
        )

        setter.resetOrDelete(alarmId, preAlarmTrigger, intake)
    }
}