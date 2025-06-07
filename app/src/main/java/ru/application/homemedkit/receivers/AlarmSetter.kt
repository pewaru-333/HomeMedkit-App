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
import ru.application.homemedkit.utils.ALARM_ID
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.expirationCheckTime
import ru.application.homemedkit.utils.extensions.canScheduleExactAlarms

class AlarmSetter(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    private val database = MedicineDatabase.getInstance(context)

    private val alarmIntent = Intent(context, AlarmReceiver::class.java).addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
    private val preAlarmIntent = Intent(context, PreAlarmReceiver::class.java).addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

    fun setAlarm(takenId: Long, trigger: Long) {
        val pending = getBroadcast(
            context,
            takenId.toInt(),
            alarmIntent.putExtra(ALARM_ID, takenId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        with(manager) {
            if (context.canScheduleExactAlarms()) {
                if (Preferences.useAlarmClock) {
                    setAlarmClock(AlarmManager.AlarmClockInfo(trigger, pending), pending)
                } else {
                    setExactAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
                }
            } else {
                setAndAllowWhileIdle(RTC_WAKEUP, trigger, pending)
            }
        }
    }

    fun setPreAlarm(intakeId: Long) {
        val alarm = database.alarmDAO().getNextByIntakeId(intakeId) ?: return
        val preTrigger = alarm.trigger - 1800000L

        val pending = getBroadcast(
            context,
            alarm.alarmId.toInt(),
            preAlarmIntent.putExtra(ALARM_ID, alarm.alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        with(manager) {
            if (context.canScheduleExactAlarms()) {
                if (Preferences.useAlarmClock) {
                    setAlarmClock(AlarmManager.AlarmClockInfo(preTrigger, pending), pending)
                } else {
                    setExactAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pending)
                }
            } else {
                setAndAllowWhileIdle(RTC_WAKEUP, preTrigger, pending)
            }
        }
    }

    fun removeAlarm(intakeId: Long) {
        val nextAlarm = database.alarmDAO().getNextByIntakeId(intakeId) ?: return

        val pendingA = getBroadcast(
            context,
            nextAlarm.alarmId.toInt(),
            preAlarmIntent.putExtra(ALARM_ID, nextAlarm.alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        val pendingB = getBroadcast(
            context,
            nextAlarm.alarmId.toInt(),
            alarmIntent.putExtra(ALARM_ID, nextAlarm.alarmId),
            FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        with(manager) {
            cancel(pendingA)
            cancel(pendingB)
        }
    }

    fun resetAll() = database.alarmDAO().getAll()
        .filter { it.trigger < System.currentTimeMillis() }
        .sortedBy(Alarm::trigger)
        .mapNotNull(Alarm::intakeId)
        .forEach(::setPreAlarm)

    fun cancelAll() = database.alarmDAO().getAll().forEach {
        with(manager) {
            cancel(
                getBroadcast(
                    context,
                    it.alarmId.toInt(),
                    preAlarmIntent.putExtra(ALARM_ID, it.alarmId),
                    FLAG_CANCEL_CURRENT or FLAG_IMMUTABLE
                )
            )

            cancel(
                getBroadcast(
                    context,
                    it.alarmId.toInt(),
                    alarmIntent.putExtra(ALARM_ID, it.alarmId),
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
            if (check) {
                if (context.canScheduleExactAlarms()) {
                    if (Preferences.useAlarmClock) {
                        setAlarmClock(AlarmManager.AlarmClockInfo(expirationCheckTime(), broadcast), broadcast)
                    } else {
                        setExactAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
                    }
                } else {
                    setAndAllowWhileIdle(RTC_WAKEUP, expirationCheckTime(), broadcast)
                }
            } else {
                cancel(broadcast)
            }
        }
    }
}