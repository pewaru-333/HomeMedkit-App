package ru.application.homemedkit.utils

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface ResourceText {
    data class StaticString(val value: String) : ResourceText

    class StringResource(
        @StringRes val resourceId: Int,
        vararg val args: Any
    ) : ResourceText

    class PluralStringResource(
        @PluralsRes val resourceId: Int,
        val count: Int,
        vararg val args: Any
    ) : ResourceText

    @Composable
    fun asString() = when (this) {
        is StaticString -> value

        is StringResource -> stringResource(
            resourceId,
            *args.map {
                if (it is StringResource) stringResource(it.resourceId, it.args)
                else it
            }.toTypedArray()
        )

        is PluralStringResource -> pluralStringResource(resourceId, count, *args)
    }
}