package ru.application.homemedkit.utils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface ResourceText {
    @Immutable
    data class StaticString(val value: String) : ResourceText

    @Immutable
    data class MultiString(val value: List<ResourceText>) : ResourceText

    @Immutable
    class StringResource(
        @param:StringRes val resourceId: Int,
        vararg val args: Any
    ) : ResourceText

    @Immutable
    class PluralStringResource(
        @param:PluralsRes val resourceId: Int,
        val count: Int,
        vararg val args: Any
    ) : ResourceText

    @Composable
    fun asString(): String = when (this) {
        is StaticString -> value

        is MultiString -> buildString {
            value.forEachIndexed { index, text ->
                append(text.asString())
                if (index < value.lastIndex) append(" ")
            }
        }

        is StringResource -> {
            if (args.isEmpty()) {
                stringResource(resourceId)
            } else {
                val mappedArgs = args.map { arg ->
                    if (arg is ResourceText) arg.asString() else arg
                }

                stringResource(resourceId, *mappedArgs.toTypedArray())
            }
        }

        is PluralStringResource -> pluralStringResource(resourceId, count, *args)
    }

    fun asString(context: Context): String = when (this) {
        is StaticString -> value

        is MultiString -> buildString {
            value.forEachIndexed { index, text ->
                append(text.asString(context))
                if (index < value.lastIndex) append(" ")
            }
        }

        is StringResource -> {
            if (args.isEmpty()) {
                context.getString(resourceId)
            } else {
                val mappedArgs = args.map { arg ->
                    if (arg is ResourceText) arg.asString(context) else arg
                }

                context.getString(resourceId, *mappedArgs.toTypedArray())
            }
        }

        is PluralStringResource -> context.resources.getQuantityString(resourceId, count, *args)
    }
}