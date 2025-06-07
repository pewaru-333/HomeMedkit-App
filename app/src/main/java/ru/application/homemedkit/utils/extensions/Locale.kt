package ru.application.homemedkit.utils.extensions

import java.util.Locale

fun Locale.getDisplayRegionName() = getDisplayName(this).replaceFirstChar(Char::uppercase)