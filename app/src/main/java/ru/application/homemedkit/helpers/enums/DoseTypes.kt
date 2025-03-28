package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R


enum class DoseTypes(@StringRes val title: Int) {
    UNKNOWN(R.string.blank),
    UNITS(R.string.dose_ed),
    PIECES(R.string.dose_pcs),
    SACHETS(R.string.dose_sach),
    GRAMS(R.string.dose_g),
    MILLIGRAMS(R.string.dose_mg),
    LITERS(R.string.dose_l),
    MILLILITERS(R.string.dose_ml),
    RATIO(R.string.dose_ratio)
}