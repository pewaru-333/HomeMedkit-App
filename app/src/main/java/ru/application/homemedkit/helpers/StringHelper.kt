package ru.application.homemedkit.helpers

import android.content.Context
import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat
import androidx.core.text.HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS
import androidx.core.text.HtmlCompat.fromHtml
import ru.application.homemedkit.R

fun formName(name: String) = name.substringBefore(" ")
fun shortName(name: String) = name.substringBefore(",")
fun fromHTML(text: String) = fromHtml(text, FROM_HTML_OPTION_USE_CSS_COLORS).toString()
fun decimalFormat(text: Any): String {
    val amount = try {
        text.toString().toDouble()
    } catch (e: NumberFormatException) {
        0.0
    }

    val formatter = DecimalFormat.getInstance()
    formatter.maximumFractionDigits = 4
    formatter.roundingMode = BigDecimal.ROUND_HALF_EVEN

    return formatter.format(amount)
}

fun intervalName(context: Context, interval: Int): String {
    val names = context.resources.getStringArray(R.array.interval_types_name)

    return when (interval) {
        1 -> names[0]
        7 -> names[1]
        else -> names[2]
    }
}