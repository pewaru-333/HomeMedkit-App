package ru.application.homemedkit.receivers

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.expirationCheckTime

class AlarmSetter(private val context: Context) {

    private val manager = context.getSystemService(AlarmManager::class.java)
    private val database = MedicineDatabase.getInstance(context)

    fun setAlarm(intakeId: Long, triggers: List<Long>) {
        triggers.forEach { trigger ->
            val alarmId = database.alarmDAO().add(Alarm(intakeId = intakeId, trigger = trigger))
            val intent = Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId)
            val pending = getBroadcast(
                context,
                alarmId.toInt(),
                intent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )

            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
        }
    }

    fun setAlarm(alarmId: Long) {
        val alarm = database.alarmDAO().getByPK(alarmId)
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId)
        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        manager.setExactAndAllowWhileIdle(RTC_WAKEUP, alarm.trigger, pending)
    }

    fun resetAlarm(alarmId: Long) {
        val alarm = database.alarmDAO().getByPK(alarmId)
        val intake = database.intakeDAO().getById(alarm.intakeId)!!
        val trigger = alarm.trigger + AlarmManager.INTERVAL_DAY * intake.interval
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId)

        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
        database.alarmDAO().reset(alarmId, trigger)
    }

    fun removeAlarm(alarmId: Long) {
        val alarm = database.alarmDAO().getByPK(alarmId)
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId)
        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        manager.cancel(pending)
        database.alarmDAO().delete(alarm)
    }

    fun resetAll() = database.alarmDAO().getAll().forEach { (alarmId, _, trigger) ->
        manager.setExactAndAllowWhileIdle(
            RTC_WAKEUP, trigger, getBroadcast(
                context,
                alarmId.toInt(),
                Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        )
    }

    fun cancelAll() = database.alarmDAO().getAll().forEach { (alarmId) ->
        manager.cancel(
            getBroadcast(
                context,
                alarmId.toInt(),
                Intent(context, AlarmReceiver::class.java).putExtra(ALARM_ID, alarmId),
                FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
            )
        )
    }

    fun checkExpiration(check: Boolean) {
        val broadcast = getBroadcast(
            context,
            81000,
            Intent(context, ExpirationReceiver::class.java),
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        if (check) manager.setExactAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
        else manager.cancel(broadcast)
    }
}