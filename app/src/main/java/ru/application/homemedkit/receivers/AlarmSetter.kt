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
import ru.application.homemedkit.helpers.AlarmType
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

    fun setAlarm(intakeId: Long, trigger: Long, amount: Double, preAlarm: Boolean) {
        val alarmId = database.alarmDAO().add(
            Alarm(
                intakeId = intakeId,
                trigger = trigger,
                amount = amount,
                preAlarm = preAlarm
            )
        )

        val preTrigger = trigger - 1800000L

        val pendingA = getBroadcast(
            context,
            alarmId.toInt(),
            preAlarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        val pendingB = getBroadcast(
            context,
            alarmId.toInt(),
            alarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (context.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
        } else {
            manager.setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
            manager.setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
        }
    }

    fun resetOrDelete(alarmId: Long, trigger: Long, intake: Intake, type: AlarmType) {
        val interval = if (intake.schemaType == SchemaTypes.PERSONAL) intake.interval
        else intake.schemaType.interval.days

        val nextTrigger = getDateTime(trigger).plusDays(interval.toLong()).toLocalDateTime()
        val lastTrigger = LocalDateTime.of(
            LocalDate.parse(intake.finalDate, FORMAT_S),
            getDateTime(trigger).toLocalTime()
        )

        if (nextTrigger >= lastTrigger) removeAlarm(alarmId, type)
        else resetAlarm(alarmId, interval, type)
    }

    fun removeAlarm(alarmId: Long, type: AlarmType) {
        val alarm = database.alarmDAO().getById(alarmId)

        val pendingA = getBroadcast(
            context,
            alarmId.toInt(),
            preAlarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        val pendingB = getBroadcast(
            context,
            alarmId.toInt(),
            alarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        when (type) {
            AlarmType.ALARM -> manager.cancel(pendingB)
            AlarmType.PREALARM -> manager.cancel(pendingA)
            AlarmType.ALL -> {
                manager.cancel(pendingA)
                manager.cancel(pendingB)
            }
        }

        if (type == AlarmType.ALARM || type == AlarmType.ALL) database.alarmDAO().delete(alarm)
    }

    fun resetAll() = database.alarmDAO().getAll().forEach { (alarmId, _, trigger, _, _) ->
        if (context.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(
                RTC_WAKEUP, trigger, getBroadcast(
                    context,
                    alarmId.toInt(),
                    alarmIntent.putExtra(ALARM_ID, alarmId),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                )
            )

            manager.setExactAndAllowWhileIdle(
                RTC_WAKEUP, trigger - 1800000L, getBroadcast(
                    context,
                    alarmId.toInt(),
                    preAlarmIntent.putExtra(ALARM_ID, alarmId),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                )
            )
        } else {
            manager.setAndAllowWhileIdle(
                RTC_WAKEUP, trigger, getBroadcast(
                    context,
                    alarmId.toInt(),
                    alarmIntent.putExtra(ALARM_ID, alarmId),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                )
            )

            manager.setAndAllowWhileIdle(
                RTC_WAKEUP, trigger - 1800000L, getBroadcast(
                    context,
                    alarmId.toInt(),
                    preAlarmIntent.putExtra(ALARM_ID, alarmId),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
                )
            )
        }
    }

    fun cancelAll() = database.alarmDAO().getAll().forEach {
        manager.cancel(
            getBroadcast(
                context,
                it.alarmId.toInt(),
                alarmIntent.putExtra(ALARM_ID, it.alarmId),
                FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
            )
        )

        manager.cancel(
            getBroadcast(
                context,
                it.alarmId.toInt(),
                preAlarmIntent.putExtra(ALARM_ID, it.alarmId),
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

    private fun resetAlarm(alarmId: Long, interval: Int, type: AlarmType) {
        val alarm = database.alarmDAO().getById(alarmId)
        val trigger = alarm.trigger + AlarmManager.INTERVAL_DAY * interval
        val preTrigger = trigger - 1800000L

        val pendingA = getBroadcast(
            context,
            alarmId.toInt(),
            preAlarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        val pendingB = getBroadcast(
            context,
            alarmId.toInt(),
            alarmIntent.putExtra(ALARM_ID, alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        if (context.canScheduleExactAlarms()) when (type) {
            AlarmType.ALARM -> manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            AlarmType.PREALARM -> manager.setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
            AlarmType.ALL -> {
                manager.setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                manager.setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            }
        } else when (type) {
            AlarmType.ALARM -> manager.setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            AlarmType.PREALARM -> manager.setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
            AlarmType.ALL -> {
                manager.setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                manager.setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            }
        }

        if (type == AlarmType.ALARM) database.alarmDAO().reset(alarmId, trigger)
    }
}