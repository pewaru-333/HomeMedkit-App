package ru.application.homemedkit.utils.extensions

import androidx.core.text.HtmlCompat

fun String.asHtml() = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString()