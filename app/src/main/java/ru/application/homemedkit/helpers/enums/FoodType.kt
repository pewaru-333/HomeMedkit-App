package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class FoodType(val value: Int, @StringRes val title: Int) {
    BEFORE(0, R.string.intake_text_food_before),
    DURING(1, R.string.intake_text_food_during),
    AFTER(2, R.string.intake_text_food_after)
}