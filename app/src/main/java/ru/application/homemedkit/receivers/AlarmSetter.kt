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
import ru.application.homemedkit.data.dto.Intake
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.FORMAT_S
import ru.application.homemedkit.helpers.SchemaTypes
import ru.application.homemedkit.helpers.canScheduleExactAlarms
import ru.application.homemedkit.helpers.expirationCheckTime
import ru.application.homemedkit.helpers.getDateTime
import java.time.LocalDate
import java.time.LocalDateTime

class AlarmSetter(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    private val database = MedicineDatabase.getInstance(context)

    private val alarmIntent = Intent(context, AlarmReceiver::class.java)
    private val preAlarmIntent = Intent(context, PreAlarmReceiver::class.java)

    fun setAlarm(intakeId: Long, trigger: Long, amount: Double, preAlarm: Boolean = false) {
        val alarmId = database.alarmDAO().add(
            Alarm(
                intakeId = intakeId,
                trigger = trigger,
                amount = amount,
                preAlarm = preAlarm
            )
        )

        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            (if (preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (context.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
        else manager.setAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
    }

    fun resetOrDelete(alarmId: Long, trigger: Long, intake: Intake) = when (val schema = intake.schemaType) {
        SchemaTypes.INDEFINITELY, SchemaTypes.BY_DAYS -> {
            val nextTrigger = getDateTime(trigger).plusDays(schema.interval.days.toLong()).toLocalDateTime()
            val lastTrigger = LocalDateTime.of(
                LocalDate.parse(intake.finalDate, FORMAT_S),
                getDateTime(trigger).toLocalTime()
            )

            if (nextTrigger >= lastTrigger) removeAlarm(alarmId)
            else resetAlarm(alarmId, schema.interval.days)
        }

        SchemaTypes.PERSONAL -> {
            val nextTrigger = getDateTime(trigger).plusDays(intake.interval.toLong()).toLocalDateTime()
            val lastTrigger = LocalDateTime.of(
                LocalDate.parse(intake.finalDate, FORMAT_S),
                getDateTime(trigger).toLocalTime()
            )

            if (nextTrigger >= lastTrigger) removeAlarm(alarmId)
            else resetAlarm(alarmId, intake.interval)
        }
    }

    fun removeAlarm(alarmId: Long) {
        val alarm = database.alarmDAO().getById(alarmId)
        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            (if (alarm.preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        manager.cancel(pending)
        database.alarmDAO().delete(alarm)
    }

    fun resetAll() = database.alarmDAO().getAll().forEach { (alarmId, _, trigger, _, preAlarm) ->
        if (context.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(
            RTC_WAKEUP, trigger, getBroadcast(
                context,
                alarmId.toInt(),
                (if (preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        )
        else manager.setAndAllowWhileIdle(
            RTC_WAKEUP, trigger, getBroadcast(
                context,
                alarmId.toInt(),
                (if (preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        )
    }

    fun cancelAll() = database.alarmDAO().getAll().forEach { (alarmId, _, _, _, preAlarm) ->
        manager.cancel(
            getBroadcast(
                context,
                alarmId.toInt(),
                (if (preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
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

        if (check)
            if (context.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
            else manager.setAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
        else manager.cancel(broadcast)
    }

    private fun resetAlarm(alarmId: Long, interval: Int) {
        val alarm = database.alarmDAO().getById(alarmId)
        val trigger = alarm.trigger + AlarmManager.INTERVAL_DAY * interval

        val pending = getBroadcast(
            context,
            alarmId.toInt(),
            (if (alarm.preAlarm) preAlarmIntent else alarmIntent).putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (context.canScheduleExactAlarms()) manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
        else manager.setAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
        database.alarmDAO().reset(alarmId, trigger)
    }
}