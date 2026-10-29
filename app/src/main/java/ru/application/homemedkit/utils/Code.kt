package ru.application.homemedkit.utils

class Code private constructor(val value: String, val identity: String, val isEan13: Boolean) {
    companion object {
        fun parse(raw: String): Code? {
            var value = raw.trim()
                .removePrefix("]d2")
                .removePrefix("{FNC1}")
                .trimStart('\u001D')
                .removePrefix("<GS>")
                .removePrefix("{GS}")
                .trimStart('\u001D')

            if (value.length == 13) {
                var sum = 0
                for (index in value.indices) {
                    val digit = value[index] - '0'
                    if (digit !in 0..9) return null
                    sum += digit * if (index % 2 == 0) 1 else 3
                }

                return if (sum % 10 != 0) null
                else Code(value, value, true)
            }

            if (value.length < 31 || !value.startsWith("01") || !value.startsWith("21", 16)) {
                return null
            }
            for (index in 2 until 16) {
                if (value[index] !in '0'..'9') return null
            }
            for (index in 18 until 31) {
                if (value[index] !in '!'..'~') return null
            }

            if (value.startsWith("<GS>", 31) || value.startsWith("{GS}", 31)) {
                value = value.replaceRange(31, 35, "\u001D")
            }
            if (value.length > 31 && value[31] != '\u001D') {
                return null
            }
            if (value.startsWith("\u001D91", 31) && (value.startsWith("<GS>", 38) || value.startsWith("{GS}", 38))) {
                value = value.replaceRange(38, 42, "\u001D")
            }

            return Code(value, value.substring(0, 31), false)
        }
    }
}
