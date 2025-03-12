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
import ru.application.homemedkit.data.dto.IntakeTaken
import ru.application.homemedkit.helpers.ALARM_ID
import ru.application.homemedkit.helpers.AlarmType
import ru.application.homemedkit.helpers.FORMAT_DD_MM_YYYY
import ru.application.homemedkit.helpers.SchemaTypes
import ru.application.homemedkit.helpers.expirationCheckTime
import ru.application.homemedkit.helpers.extensions.canScheduleExactAlarms
import ru.application.homemedkit.helpers.getDateTime
import java.time.LocalDate
import java.time.LocalDateTime

class AlarmSetter(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    private val database = MedicineDatabase.getInstance(context)

    private val alarmIntent = Intent(context, AlarmReceiver::class.java).addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
    private val preAlarmIntent = Intent(context, PreAlarmReceiver::class.java).addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

    suspend fun setAlarm(intakeId: Long, trigger: Long, amount: Double, preAlarm: Boolean) {
        val alarmId = database.alarmDAO().insert(
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

        with(manager) {
            if (context.canScheduleExactAlarms()) {
                setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            } else {
                setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
            }
        }
    }

    suspend fun resetOrDelete(alarmId: Long, trigger: Long, intake: Intake, type: AlarmType) {
        val interval = if (intake.schemaType == SchemaTypes.PERSONAL) intake.interval
        else intake.schemaType.interval.days

        val nextTrigger = getDateTime(trigger).plusDays(interval.toLong()).toLocalDateTime()
        val lastTrigger = LocalDateTime.of(
            LocalDate.parse(intake.finalDate, FORMAT_DD_MM_YYYY),
            getDateTime(trigger).toLocalTime()
        )

        if (nextTrigger > lastTrigger) removeAlarm(alarmId, type)
        else resetAlarm(alarmId, interval, type)
    }

    suspend fun removeAlarm(alarmId: Long, type: AlarmType) {
        val alarm = database.alarmDAO().getById(alarmId) ?: return

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

        with(manager) {
            when (type) {
                AlarmType.ALARM -> cancel(pendingB)
                AlarmType.PREALARM -> cancel(pendingA)
                AlarmType.ALL -> {
                    cancel(pendingA)
                    cancel(pendingB)
                }
            }
        }

        if (type == AlarmType.ALARM || type == AlarmType.ALL) database.alarmDAO().delete(alarm)
    }

    fun resetAll() {
        val takenAlarmIdList = database.takenDAO().getAll().map(IntakeTaken::alarmId)

        database.alarmDAO().getAll().forEach {
            val preTrigger = it.trigger - 1800000L

            val pendingA = getBroadcast(
                context,
                it.alarmId.toInt(),
                alarmIntent.putExtra(ALARM_ID, it.alarmId),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )

            val pendingB = getBroadcast(
                context,
                it.alarmId.toInt(),
                preAlarmIntent.putExtra(ALARM_ID, it.alarmId),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )

            with(manager) {
                if (context.canScheduleExactAlarms()) {
                    setExactAndAllowWhileIdle(RTC_WAKEUP, it.trigger, pendingA)
                    if (it.alarmId !in takenAlarmIdList)
                        setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingB)
                } else {
                    setAndAllowWhileIdle(RTC_WAKEUP, it.trigger, pendingA)
                    if (it.alarmId !in takenAlarmIdList)
                        setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingB)
                }
            }
        }
    }

    fun cancelAll() = database.alarmDAO().getAll().forEach {
        with(manager) {
            cancel(
                getBroadcast(
                    context,
                    it.alarmId.toInt(),
                    alarmIntent.putExtra(ALARM_ID, it.alarmId),
                    FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
                )
            )

            cancel(
                getBroadcast(
                    context,
                    it.alarmId.toInt(),
                    preAlarmIntent.putExtra(ALARM_ID, it.alarmId),
                    FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
                )
            )
        }
    }

    fun checkExpiration(check: Boolean) {
        val broadcast = getBroadcast(
            context,
            81000,
            Intent(context, ExpirationReceiver::class.java).addFlags(Intent.FLAG_RECEIVER_FOREGROUND),
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
        )

        with(manager) {
            if (check)
                if (context.canScheduleExactAlarms()) setExactAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
                else setAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
            else cancel(broadcast)
        }
    }

    private fun resetAlarm(alarmId: Long, interval: Int, type: AlarmType) {
        val alarm = database.alarmDAO().getById(alarmId) ?: return
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

        with(manager) {
            if (context.canScheduleExactAlarms()) when (type) {
                AlarmType.ALARM -> setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
                AlarmType.PREALARM -> setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                AlarmType.ALL -> {
                    setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                    setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
                }
            } else when (type) {
                AlarmType.ALARM -> setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
                AlarmType.PREALARM -> setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                AlarmType.ALL -> {
                    setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pendingA)
                    setAndAllowWhileIdle(RTC_WAKEUP, trigger, pendingB)
                }
            }
        }

        if (type == AlarmType.ALARM) database.alarmDAO().reset(alarmId, trigger)
    }
}