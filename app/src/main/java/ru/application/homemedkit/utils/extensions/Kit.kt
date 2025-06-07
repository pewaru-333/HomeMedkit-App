package ru.application.homemedkit.utils.extensions

import ru.application.homemedkit.R
import ru.application.homemedkit.data.dto.Kit
import ru.application.homemedkit.data.model.KitModel
import ru.application.homemedkit.utils.ResourceText

fun Kit.toModel() = KitModel(
    id = kitId,
    position = position,
    title = if (kitId > 0) ResourceText.StaticString(title)
    else ResourceText.StringResource(R.string.text_no_group)
)