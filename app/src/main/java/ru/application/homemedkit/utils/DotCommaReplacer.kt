package ru.application.homemedkit.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object DotCommaReplacer : VisualTransformation {
    override fun filter(text: AnnotatedString) = TransformedText(
        text = AnnotatedString(text.text.replace('.', ',')),
        offsetMapping = OffsetMapping.Identity
    )
}