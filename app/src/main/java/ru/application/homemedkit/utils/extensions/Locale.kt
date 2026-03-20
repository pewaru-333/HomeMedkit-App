package ru.application.homemedkit.utils.extensions

import java.util.Locale

fun Locale.getLocalizedName() = getDisplayName(this).replaceFirstChar(Char::uppercase)