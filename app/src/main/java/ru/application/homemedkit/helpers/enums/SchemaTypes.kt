package ru.application.homemedkit.helpers.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class SchemaTypes(
    @StringRes val title: Int,
    val interval: Intervals
) {
    INDEFINITELY(
        title = R.string.intake_schema_type_indefinitely,
        interval = Intervals.DAILY
    ),
    BY_DAYS(
        title = R.string.intake_schema_type_day_picker,
        interval = Intervals.WEEKLY
    ),
    PERSONAL(
        title = R.string.intake_schema_type_personal,
        interval = Intervals.CUSTOM
    )
}