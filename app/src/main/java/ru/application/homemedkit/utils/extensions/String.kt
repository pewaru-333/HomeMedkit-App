package ru.application.homemedkit.utils.extensions

import androidx.core.text.HtmlCompat
import java.security.MessageDigest

fun String.asHtml() = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString()

@OptIn(ExperimentalStdlibApi::class)
fun String.toSHA256() = MessageDigest.getInstance("SHA-256").digest(toByteArray()).toHexString()