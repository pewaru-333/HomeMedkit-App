package ru.application.homemedkit.utils

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.network.IProgress
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.DrugType
import java.io.File

suspend fun getMedicineImages(
    medicineId: Long,
    form: String,
    directory: File,
    urls: List<String>?,
    onProgress: IProgress?
): List<Image> {
    val imageList = if (Preferences.imageFetch && !urls.isNullOrEmpty()) {
        Network.getImage(directory, urls, onProgress)
    } else {
        emptyList()
    }

    return imageList.mapIndexed { index, imageName ->
        Image(
            medicineId = medicineId,
            position = index,
            image = imageName
        )
    }.ifEmpty {
        listOf(
            Image(
                medicineId = medicineId,
                image = DrugType.setIcon(form)
            )
        )
    }
}

fun shiftCipher(input: String, shift: Int = 10) = CharArray(input.length) { input[it] + shift }.concatToString()

object DecimalAmountInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        for (i in length - 1 downTo 0) {
            if (charAt(i) == ',') {
                replace(i, i + 1, ".")
            }
        }

        val text = toString()
        if (text.isNotEmpty()) {
            if (text.toDoubleOrNull() == null) {
                revertAllChanges()
            }
        }
    }
}

object DecimalAmountOutputTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        for (i in length - 1 downTo 0) {
            if (charAt(i) == '.') {
                replace(i, i + 1, ",")
            }
        }
    }
}

object DaysInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val text = toString()
        if (text.isNotEmpty()) {
            val days = text.toIntOrNull()

            if (days == null || days <= 0) {
                revertAllChanges()
            }
        }
    }
}