package ru.application.homemedkit.helpers

import android.content.Context
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import androidx.core.text.HtmlCompat.fromHtml
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import ru.application.homemedkit.R
import ru.application.homemedkit.helpers.ConstantsHelper.BLANK
import ru.application.homemedkit.helpers.ConstantsHelper.DOWN_DASH
import ru.application.homemedkit.helpers.ConstantsHelper.NUMBER_FORMAT
import ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON
import ru.application.homemedkit.helpers.ConstantsHelper.WHITESPACE
import ru.application.homemedkit.helpers.ConstantsHelper.WHITESPACE_R
import java.text.ParseException
import java.util.Arrays

fun formName(name: String) = name.substringBefore(WHITESPACE)

fun shortName(name: String) = name.substringBefore(SEMICOLON)

fun fromHTML(text: String) = fromHtml(text, FROM_HTML_OPTION_USE_CSS_COLORS).toString()

fun daysInterval(interval: String) = interval.substringAfter(DOWN_DASH).toInt()

fun decimalFormat(amount: Double): String = NUMBER_FORMAT.format(amount)

fun parseAmount(input: String): Double = try {
    NUMBER_FORMAT.parse(input).toDouble()
} catch (e: ParseException) {
    0.0
} catch (e: NullPointerException) {
    0.0
}

fun intervalName(context: Context, interval: String): String {
    val types = context.resources.getStringArray(R.array.interval_types)
    val names = context.resources.getStringArray(R.array.interval_types_name)

    return when (interval) {
        types[0] -> names[0]
        types[1] -> names[1]
        types[2] -> names[2]
        else -> names[3]
    }
}

fun timesString(layout: FlexboxLayout): String {
    val childCount = layout.childCount
    val times = StringBuilder(childCount * 7)

    for (child in 0..<childCount)
        times.append((layout.getChildAt(child) as Chip).text).append(SEMICOLON)
    times.deleteCharAt(times.length - 1)

    val sorted = Arrays.toString(DateHelper.sortTimes(times.toString().trim()))

    return sorted.substring(1, sorted.length - 1).replace(WHITESPACE_R.toRegex(), BLANK)
}