package ru.application.homemedkit.utils.enums

import androidx.annotation.StringRes
import ru.application.homemedkit.R


enum class IntakeExtra(
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    CANCELLABLE(
        title = R.string.intake_extra_cancellable,
        description = R.string.intake_extra_desc_cancellable
    ),
    FULLSCREEN(
        title = R.string.intake_extra_fullscreen,
        description = R.string.intake_extra_desc_fullscreen
    ),
    NO_SOUND(
        title = R.string.intake_extra_no_sound,
        description = R.string.intake_extra_desc_no_sound
    ),
    PREALARM(
        title = R.string.intake_extra_prealarm,
        description = R.string.intake_extra_desc_prealarm
    )
}