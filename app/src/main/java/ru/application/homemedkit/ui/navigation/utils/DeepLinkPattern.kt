package ru.application.homemedkit.ui.navigation.utils

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialKind
import java.io.Serializable

internal class DeepLinkPattern<T : NavKey>(val serializer: KSerializer<T>, val uriPattern: Uri) {
    private val regexPatternFillIn = Regex("\\{(.+?)\\}")

    val pathSegments: List<PathSegment> by lazy {
        buildList {
            uriPattern.pathSegments.forEach { segment ->
                var result = regexPatternFillIn.find(segment)
                if (result != null) {
                    val argName = result.groups[1]!!.value
                    val elementIndex = serializer.descriptor.getElementIndex(argName)
                    val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)

                    add(PathSegment(argName, true, getTypeParser(elementDescriptor.kind)))
                } else {
                    add(PathSegment(segment, false, getTypeParser(PrimitiveKind.STRING)))
                }
            }
        }
    }

    val queryValueParsers: Map<String, TypeParser> by lazy {
        buildMap {
            uriPattern.queryParameterNames.forEach { paramName ->
                val elementIndex = serializer.descriptor.getElementIndex(paramName)
                val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
                put(paramName, getTypeParser(elementDescriptor.kind))
            }
        }
    }

    class PathSegment(
        val stringValue: String,
        val isParamArg: Boolean,
        val typeParser: TypeParser
    )
}

private typealias TypeParser = (String) -> Serializable

private fun getTypeParser(kind: SerialKind): TypeParser = when (kind) {
    PrimitiveKind.STRING -> Any::toString
    PrimitiveKind.INT -> String::toInt
    PrimitiveKind.BOOLEAN -> String::toBoolean
    PrimitiveKind.BYTE -> String::toByte
    PrimitiveKind.CHAR -> String::toCharArray
    PrimitiveKind.DOUBLE -> String::toDouble
    PrimitiveKind.FLOAT -> String::toFloat
    PrimitiveKind.LONG -> String::toLong
    PrimitiveKind.SHORT -> String::toShort
    else -> throw IllegalArgumentException()
}