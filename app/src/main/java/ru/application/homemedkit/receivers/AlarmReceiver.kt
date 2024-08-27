package ru.application.homemedkit.receivers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
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
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R.drawable.vector_time
import ru.application.homemedkit.R.string.intake_text_not_taken
import ru.application.homemedkit.R.string.intake_text_taken
import ru.application.homemedkit.R.string.notification_channel_description
import ru.application.homemedkit.R.string.notification_channel_name
import ru.application.homemedkit.R.string.text_do_intake
import ru.application.homemedkit.R.string.text_intake_amount_not_enough
import ru.application.homemedkit.R.string.text_intake_time
import ru.application.homemedkit.data.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.BLANK
import ru.application.homemedkit.helpers.CHANNEL_ID
import ru.application.homemedkit.helpers.ID
import ru.application.homemedkit.helpers.SOUND_GROUP
import ru.application.homemedkit.helpers.TAKEN_ID
import ru.application.homemedkit.helpers.TYPE
import ru.application.homemedkit.helpers.lastAlarm
import ru.application.homemedkit.helpers.shortName

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val database = getInstance(context)
        val setter = AlarmSetter(context)

        val medicineDAO = database.medicineDAO()
        val intakeDAO = database.intakeDAO()
        val alarmDAO = database.alarmDAO()

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)
        val intakeId = alarmDAO.getByPK(alarmId).intakeId
        val intake = intakeDAO.getById(intakeId)!!
        val medicineId = intake.medicineId

        val medicine = medicineDAO.getById(medicineId)!!
        val trigger = alarmDAO.getByPK(alarmId).trigger
        val flag = medicineDAO.getProdAmount(medicineId) >= intake.amount

        val intakeTaken = IntakeTaken(
            medicineId = medicineId,
            intakeId = intakeId,
            alarmId = alarmId,
            productName = medicine.productName,
            formName = medicine.prodFormNormName,
            amount = intake.amount,
            doseType = medicine.doseType,
            image = medicine.image,
            trigger = trigger,
            taken = false,
            notified = false
        )
        val takenId = database.takenDAO().add(intakeTaken)

        val action = Intent(context, ActionReceiver::class.java).apply {
            putExtra(ID, medicineId)
            putExtra(TAKEN_ID, takenId)
            putExtra(BLANK, intake.amount)
        }
        val pendingA = getBroadcast(context, takenId.toInt(), action, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        val pendingB = getBroadcast(context, takenId.toInt(), action.setAction(TYPE), FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)

        createNotificationChannel(context)
        getRingtone(context, getDefaultUri(TYPE_NOTIFICATION)).play()
        NotificationManagerCompat.from(context).notify(
            takenId.toInt(),
            Builder(context, CHANNEL_ID).apply {
                if (flag) {
                    addAction(vector_time, context.getString(intake_text_taken), pendingB)
                    addAction(vector_time, context.getString(intake_text_not_taken), pendingA)
                }
            }
                .setAutoCancel(false)
                .setCategory(CATEGORY_ALARM)
                .setContentTitle(context.getString(text_do_intake))
                .setDefaults(Notification.DEFAULT_ALL)
                .setDeleteIntent(pendingA)
                .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
                .setGroup(SOUND_GROUP)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setSmallIcon(vector_time)
                .setStyle(BigTextStyle().bigText(
                    String.format(
                        context.getString(if (flag) text_intake_time else text_intake_amount_not_enough),
                        shortName(medicine.productName), intake.amount, medicine.doseType
                    )
                ))
                .setTimeoutAfter(600000L)
                .build()
        )

        if (trigger == lastAlarm(intake.finalDate, intake.time.last())) setter.removeAlarm(alarmId)
        else setter.resetAlarm(alarmId)
    }
}

fun createNotificationChannel(context: Context) {
    val name = context.getString(notification_channel_name)
    val description = context.getString(notification_channel_description)

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.deleteNotificationChannel(context.getString(notification_channel_name))
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
            this.description = description
        }
    )
}