package ru.application.homemedkit.alarms

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ru.application.homemedkit.R
import ru.application.homemedkit.activities.MainActivity
import ru.application.homemedkit.databaseController.Intake
import ru.application.homemedkit.databaseController.MedicineDatabase.Companion.getInstance
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.BOUND
import ru.application.homemedkit.helpers.DateHelper
import ru.application.homemedkit.helpers.NEW_INTAKE
import ru.application.homemedkit.helpers.SOUND_GROUP
import ru.application.homemedkit.helpers.shortName
import java.util.Random

class AlarmReceiver : BroadcastReceiver() {

    private fun intakeNotification(context: Context, medicineId: Long, flag: Boolean): Notification {
        val database = getInstance(context)
        val productName = database.medicineDAO().getProductName(medicineId)
        val code = Random().nextInt(BOUND)
        var title = context.getString(R.string.text_intake_time) + shortName(productName)

        if (!flag) title += context.getString(R.string.text_medicine_amount_not_enough)
        val intent = Intent(context, MainActivity::class.java)

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(NEW_INTAKE, true)

        val pending = PendingIntent.getActivity(
            context, code, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(
            context,
            context.getString(R.string.notification_channel_name)
        )
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
            .setContentIntent(pending)
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
        val compat: NotificationManagerCompat

        val alarmId = intent.getLongExtra(ALARM_ID, 0L)
        val alarm = alarmDAO.getByPK(alarmId)
        val finish = intakeDAO.getByPK(alarm.intakeId)!!.finalDate

        if (DateHelper.lastDay(finish) > System.currentTimeMillis()) {
            val intakeId = alarmDAO.getByPK(alarmId).intakeId
            val medicineId = intakeDAO.getByPK(intakeId)!!.medicineId
            val amount = intakeDAO.getByPK(intakeId)!!.amount

            if (medicineDAO.getProdAmount(medicineId) > amount) {
                medicineDAO.intakeMedicine(medicineId, amount)
                notification = intakeNotification(context, medicineId, true)
                compat = NotificationManagerCompat.from(context)

                alarmSetter.resetAlarm(alarmId)
            } else {
                notification = intakeNotification(context, medicineId, false)
                compat = NotificationManagerCompat.from(context)
                intakeDAO.remove(Intake(intakeId))
            }

            playSound(context)
            compat.notify(Random().nextInt(BOUND), notification)
        } else {
            alarmSetter.removeAlarm(alarmId)
        }
    }

    private fun playSound(context: Context) {
        RingtoneManager.getRingtone(
            context,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ).play()
    }
}