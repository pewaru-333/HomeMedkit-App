package ru.application.homemedkit.helpers

import android.content.Context
import android.icu.text.NumberFormat
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import androidx.core.text.HtmlCompat.fromHtml
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.DOWN_DASH
import ru.application.homemedkit.helpers.ConstantsHelper.INTERVALS
import ru.application.homemedkit.helpers.ConstantsHelper.NUMBER_FORMAT
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.ConstantsHelper.WHITESPACE
import ru.application.homemedkit.helpers.ConstantsHelper.WHITESPACE_R
import java.text.ParseException
import java.util.Arrays
import java.util.Locale

fun formName(name: String) = name.substringBefore(WHITESPACE)

fun shortName(name: String) = name.substringBefore(SEMICOLON)

fun fromHTML(text: String) = fromHtml(text, FROM_HTML_OPTION_USE_CSS_COLORS).toString()

fun daysInterval(interval: String): Int {
    return try {
        interval.substringAfter(DOWN_DASH).toInt()
    } catch (e: NumberFormatException) {
        1
    }
}

fun decimalFormat(amount: Double): String = NUMBER_FORMAT.format(amount)
fun decimalFormat(amount: String): Double {
    return if (amount.contains(SEMICOLON))
        NUMBER_FORMAT.parse(amount).toDouble()
    else NumberFormat.getInstance(Locale.US).parse(amount).toDouble()
}

fun parseAmount(input: String): Double = try {
    NUMBER_FORMAT.parse(input).toDouble()
} catch (e: ParseException) {
    0.0
} catch (e: NullPointerException) {
    0.0
}

fun intervalName(context: Context, interval: String): String {
    val names = context.resources.getStringArray(R.array.interval_types_name)

    return when (interval) {
        INTERVALS[0] -> names[0]
        INTERVALS[1] -> names[1]
        else -> names[2]
    }
}

fun timesString(list: List<String>): String {
    val times = StringBuilder(list.size * 7)

    list.forEach { times.append(it).append(SEMICOLON) }

    times.deleteCharAt(times.length - 1)

    val sorted = Arrays.toString(DateHelper.sortTimes(times.toString().trim()))

    return sorted.substring(1, sorted.length - 1).replace(WHITESPACE_R.toRegex(), BLANK)
}