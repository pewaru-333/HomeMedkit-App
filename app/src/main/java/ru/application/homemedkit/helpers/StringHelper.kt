package ru.application.homemedkit.helpers

import android.icu.math.BigDecimal
import android.icu.text.DecimalFormat

fun formName(name: String) = name.substringBefore(" ")
fun shortName(name: String?) = name?.substringBefore(",") ?: BLANK
fun decimalFormat(text: Any?): String {
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