package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R

enum class SchemaType(
    @StringRes val title: Int,
    val interval: Interval
) {
    INDEFINITELY(
        title = R.string.intake_schema_type_indefinitely,
        interval = Interval.DAILY
    ),
    BY_DAYS(
        title = R.string.intake_schema_type_day_picker,
        interval = Interval.WEEKLY
    ),
    PERSONAL(
        title = R.string.intake_schema_type_personal,
        interval = Interval.CUSTOM
    )
}