package ru.application.homemedkit.utils

class Code private constructor(val value: String, val identity: String, val type: Type) {
    enum class Type { EAN_13, DATA_MATRIX, AIC }

    val isEan13: Boolean
        get() = type == Type.EAN_13

    companion object {
        // Code 32 (Italian pharmacode) base-32 alphabet: digits and consonants, no vowels
        private const val CODE_32_ALPHABET = "0123456789BCDFGHJKLMNPQRSTUVWXYZ"

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
                else Code(value, value, Type.EAN_13)
            }

            parseAic(value)?.let { return Code(it, it, Type.AIC) }

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

            return Code(value, value.substring(0, 31), Type.DATA_MATRIX)
        }

        /**
         * Italian AIC code from the "bollino farmaceutico" (Code 32).
         * Accepts "A" + 9 digits, 9 digits or the raw 6-char base-32 barcode text.
         * Returns the 9-digit AIC if the check digit is valid.
         */
        private fun parseAic(value: String): String? {
            val digits = when {
                value.length == 10 && value[0] == 'A' -> value.substring(1)
                value.length == 9 -> value
                value.length == 6 -> {
                    var number = 0L
                    for (char in value) {
                        val index = CODE_32_ALPHABET.indexOf(char)
                        if (index < 0) return null
                        number = number * 32 + index
                    }
                    number.toString().padStart(9, '0')
                }
                else -> return null
            }

            if (digits.length != 9 || digits.any { it !in '0'..'9' }) return null

            var sum = 0
            for (index in 0 until 8) {
                val digit = digits[index] - '0'
                sum += if (index % 2 == 0) digit else (digit * 2).let { it / 10 + it % 10 }
            }

            return if (sum % 10 == digits[8] - '0') digits else null
        }
    }
}
