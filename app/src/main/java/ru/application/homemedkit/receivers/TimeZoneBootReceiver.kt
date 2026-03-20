package ru.application.homemedkit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.application.homemedkit.data.MedicineDatabase
import ru.application.homemedkit.data.dto.Alarm
import ru.application.homemedkit.utils.Formatter
import ru.application.homemedkit.utils.Preferences
import ru.application.homemedkit.utils.extensions.goAsync
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime

class TimeZoneBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIMEZONE_CHANGED || intent.action == Intent.ACTION_TIME_CHANGED) {
            val alarmSetter = AlarmSetter.getInstance(context)
            val database = MedicineDatabase.getInstance(context)
            val preferences = Preferences.getInstance(context)

            alarmSetter.cancelAll()

            if (preferences.autoIntakeReschedule && (intent.action == Intent.ACTION_TIMEZONE_CHANGED || intent.action == Intent.ACTION_TIME_CHANGED)) {
                val current = ZonedDateTime.now()
                val intakes = database.intakeDAO().getAll()

                for (item in intakes) {
                    val fullIntake = database.intakeDAO().getById(item.intakeId) ?: continue

                    if (fullIntake.pickedTime.isEmpty()) continue

                    val sortedTimes = fullIntake.pickedTime.sortedBy {
                        LocalTime.parse(it.time, Formatter.FORMAT_H_MM)
                    }

                    val lastTimeStr = sortedTimes.last().time
                    val finalDateTime = LocalDateTime.of(
                        LocalDate.parse(fullIntake.finalDate, Formatter.FORMAT_DD_MM_YYYY),
                        LocalTime.parse(lastTimeStr, Formatter.FORMAT_H_MM)
                    )

                    if (finalDateTime >= LocalDateTime.now()) {
                        val startDate = LocalDate.parse(fullIntake.startDate, Formatter.FORMAT_DD_MM_YYYY)
                        val finalDate = LocalDate.parse(fullIntake.finalDate, Formatter.FORMAT_DD_MM_YYYY)

                        val scheduled = mutableListOf<Alarm>()

                        val takenTriggers = database.takenDAO().getTakenTriggers(fullIntake.intakeId).toSet()

                        val missedDays = if (fullIntake.interval > 0) fullIntake.interval.toLong() else 1L
                        val windowMillis = current.minusDays(missedDays).toInstant().toEpochMilli()

                        sortedTimes.forEach { pickedTime ->
                            val localTime = LocalTime.parse(pickedTime.time, Formatter.FORMAT_H_MM)

                            var initial = ZonedDateTime.of(startDate, localTime, Formatter.ZONE)
                            val finish = ZonedDateTime.of(finalDate, localTime, Formatter.ZONE)
                            val step = if (fullIntake.interval > 0) fullIntake.interval.toLong() else 1L

                            while (!initial.isAfter(finish)) {
                                if (initial.dayOfWeek in fullIntake.pickedDays) {
                                    val triggerMillis = initial.toInstant().toEpochMilli()

                                    if (initial.isAfter(current)) {
                                        if (!takenTriggers.contains(triggerMillis)) {
                                            break
                                        }
                                    } else {
                                        if (!takenTriggers.contains(triggerMillis) && triggerMillis >= windowMillis) {
                                            break
                                        }
                                    }
                                }

                                initial = initial.plusDays(step)
                            }

                            while (!initial.isAfter(finish)) {
                                if (initial.dayOfWeek in fullIntake.pickedDays) {
                                    scheduled.add(
                                        Alarm(
                                            intakeId = fullIntake.intakeId,
                                            trigger = initial.toInstant().toEpochMilli(),
                                            amount = pickedTime.amount,
                                            preAlarm = fullIntake.preAlarm
                                        )
                                    )
                                }
                                initial = initial.plusDays(step)
                            }
                        }

                        database.alarmDAO().deleteByIntakeId(fullIntake.intakeId)

                        if (scheduled.isNotEmpty()) {
                            database.alarmDAO().insert(scheduled.sortedBy(Alarm::trigger))
                        }

                        alarmSetter.setPreAlarm(fullIntake.intakeId)
                    } else {
                        database.alarmDAO().deleteByIntakeId(fullIntake.intakeId)
                    }
                }
            }

            alarmSetter.resetAll()

            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                database.takenDAO().setNotified()
            }

            if (preferences.checkExpiration) {
                alarmSetter.checkExpiration(true)
            }
        }
    }
}