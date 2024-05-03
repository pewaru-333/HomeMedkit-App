package ru.application.homemedkit.alarms

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.media.RingtoneManager.getRingtone
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MainActivity
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.NEW_INTAKE
import ru.application.homemedkit.helpers.SOUND_GROUP
import ru.application.homemedkit.helpers.shortName

class AlarmReceiver : BroadcastReceiver() {
    private fun intakeNotification(context: Context, medicineId: Long, flag: Boolean): Notification {
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .putExtra(NEW_INTAKE, true)
        val productName = getInstance(context).medicineDAO().getProductName(medicineId)
        val title = String.format(context.getString(if (flag) R.string.text_intake_time
        else R.string.text_medicine_amount_not_enough), shortName(productName))

        val pending = getActivity(
            context,
            medicineId.toInt(),
            intent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
            .setSmallIcon(R.drawable.vector_time)
            .setContentTitle(context.getString(R.string.text_do_intake))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setGroup(SOUND_GROUP)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
           // .setContentIntent(pending)
            .build()
    }

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

        if (DateHelper.lastDay(finish) > System.currentTimeMillis()) {
            val intakeId = alarmDAO.getByPK(alarmId).intakeId
            val medicineId = intakeDAO.getByPK(intakeId)!!.medicineId
            val amount = intakeDAO.getByPK(intakeId)!!.amount

            if (medicineDAO.getProdAmount(medicineId) >= amount) {
                medicineDAO.intakeMedicine(medicineId, amount)
                notification = intakeNotification(context, medicineId, true)
                alarmSetter.resetAlarm(alarmId)
            } else {
                notification = intakeNotification(context, medicineId, false)
                intakeDAO.remove(Intake(intakeId))
            }

            playSound(context)
            NotificationManagerCompat.from(context).notify(intakeId.toInt(), notification)
        } else {
            alarmSetter.removeAlarm(alarmId)
        }
    }

    private fun playSound(context: Context) = getRingtone(context, getDefaultUri(TYPE_NOTIFICATION)).play()
}