package ru.application.homemedkit.ui.navigation.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class KeyDecoder(private val arguments: Map<String, Any>) : AbstractDecoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule()
    private var elementIndex: Int = -1
    private var elementName: String = ""

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        var currentIndex = elementIndex
        while (true) {
            currentIndex++

            if (currentIndex >= descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
            val currentName = descriptor.getElementName(currentIndex)

            if (arguments.contains(currentName)) {
                elementIndex = currentIndex
                elementName = currentName
                return elementIndex
            }
        }
    }

    override fun decodeValue(): Any {
        val arg = arguments[elementName]
        checkNotNull(arg) { "Unexpected null value for non-nullable argument $elementName" }
        return arg
    }

    override fun decodeNull(): Nothing? = null

    override fun decodeNotNullMark(): Boolean = arguments[elementName] != null
}