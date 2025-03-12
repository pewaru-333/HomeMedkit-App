package ru.application.homemedkit.helpers.extensions

import java.util.Locale

fun Locale.getDisplayRegionName() = getDisplayName(this).replaceFirstChar(Char::uppercase)