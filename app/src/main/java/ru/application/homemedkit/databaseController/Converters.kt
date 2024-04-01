package ru.application.homemedkit.databaseController

import androidx.room.TypeConverter
import ru.application.homemedkit.helpers.DateHelper.FORMAT_H
import ru.application.homemedkit.helpers.SEMICOLON
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromTimeList(times: List<LocalTime>): String {
        return times
            .sortedWith(compareBy { it })
            .joinToString(
                separator = SEMICOLON,
                transform = { it.format(FORMAT_H) }
            )
    }

    @TypeConverter
    fun toTimeList(times: String): List<LocalTime> {
        return times
            .split(SEMICOLON)
            .sortedWith(compareBy { LocalTime.parse(it, FORMAT_H) })
            .map { LocalTime.parse(it, FORMAT_H) }
    }
}