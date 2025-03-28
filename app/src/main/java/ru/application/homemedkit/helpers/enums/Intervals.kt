package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class Intervals(val days: Int, @StringRes val title: Int) {
    DAILY(1, R.string.intake_interval_daily),
    WEEKLY(7, R.string.intake_interval_weekly),
    CUSTOM(10, R.string.intake_interval_other);

    companion object {
        fun getValue(days: Int) = entries.find { it.days == days } ?: CUSTOM
        fun getTitle(days: String) = try {
            when (days.toInt()) {
                1 -> DAILY.title
                7 -> WEEKLY.title
                else -> CUSTOM.title
            }
        } catch (_: NumberFormatException) {
            R.string.blank
        }
    }
}