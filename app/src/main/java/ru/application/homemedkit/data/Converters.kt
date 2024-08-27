package ru.application.homemedkit.data

import androidx.room.TypeConverter
import ru.application.homemedkit.helpers.FORMAT_H
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromTimeList(times: List<LocalTime>) = times.sortedWith(compareBy { it })
        .joinToString(separator = ",", transform = { it.format(FORMAT_H) })

    @TypeConverter
    fun toTimeList(times: String) = times.split(",")
        .sortedWith(compareBy { LocalTime.parse(it, FORMAT_H) })
        .map { LocalTime.parse(it, FORMAT_H) }
}