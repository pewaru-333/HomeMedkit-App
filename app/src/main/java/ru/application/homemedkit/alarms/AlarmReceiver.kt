package ru.application.homemedkit.alarms

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.media.RingtoneManager.getRingtone
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.CATEGORY_ALARM
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.CHANNEL_ID
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.SOUND_GROUP
import ru.application.homemedkit.helpers.shortName

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)
        val alarmSetter = AlarmSetter(context)

        val medicineDAO = database.medicineDAO()
        val intakeDAO = database.intakeDAO()
        val alarmDAO = database.alarmDAO()

        val notification: Notification

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)
        val alarm = alarmDAO.getByPK(alarmId)
        val finish = intakeDAO.getByPK(alarm.intakeId)!!.finalDate

        createNotificationChannel(context)

        if (DateHelper.lastDay(finish) > System.currentTimeMillis()) {
            val intakeId = alarmDAO.getByPK(alarmId).intakeId
            val medicineId = intakeDAO.getByPK(intakeId)!!.medicineId
            val amount = intakeDAO.getByPK(intakeId)!!.amount

            if (medicineDAO.getProdAmount(medicineId) >= amount) {
                medicineDAO.intakeMedicine(medicineId, amount)
                notification = intakeNotification(context, intakeId, true)
                alarmSetter.resetAlarm(alarmId)
            } else {
                notification = intakeNotification(context, intakeId, false)
                intakeDAO.remove(Intake(intakeId))
            }

            playSound(context)
            NotificationManagerCompat.from(context).notify(intakeId.toInt(), notification)
        } else {
            alarmSetter.removeAlarm(alarmId)
        }
    }
}

private fun intakeNotification(context: Context, intakeId: Long, flag: Boolean): Notification {
    val database = getInstance(context)
    val intake = database.intakeDAO().getByPK(intakeId)!!
    val medicine = database.medicineDAO().getByPK(intake.medicineId)!!

    val title = String.format(context.getString(if (flag) R.string.text_intake_time
    else R.string.text_medicine_amount_not_enough),
        shortName(medicine.productName), intake.amount, medicine.doseType)

    return Builder(context, CHANNEL_ID)
        .setAutoCancel(true)
        .setCategory(CATEGORY_ALARM)
        .setContentTitle(context.getString(R.string.text_do_intake))
        .setDefaults(Notification.DEFAULT_ALL)
        .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        .setGroup(SOUND_GROUP)
        .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
        .setPriority(PRIORITY_HIGH)
        .setSmallIcon(R.drawable.vector_time)
        .setStyle(BigTextStyle().bigText(title))
        .build()
}

fun createNotificationChannel(context: Context) {
    val name = context.getString(R.string.notification_channel_name)
    val description = context.getString(R.string.notification_channel_description)

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.deleteNotificationChannel(context.getString(R.string.notification_channel_name))
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
            this.description = description
        }
    )
}

fun playSound(context: Context) = getRingtone(context, getDefaultUri(TYPE_NOTIFICATION)).play()