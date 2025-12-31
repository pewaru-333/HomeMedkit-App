package ru.application.homemedkit.utils

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.delete
import ru.application.homemedkit.data.dto.Image
import ru.application.homemedkit.network.Network
import ru.application.homemedkit.utils.di.Preferences
import ru.application.homemedkit.utils.enums.DrugType
import java.io.File

suspend fun getMedicineImages(
    medicineId: Long,
    form: String,
    directory: File,
    urls: List<String>?
): List<Image> {
    val imageList = if (Preferences.imageFetch && !urls.isNullOrEmpty()) {
        Network.getImage(directory, urls)
    } else {
        emptyList()
    }

    return if (imageList.isEmpty()) listOf(
        Image(
            medicineId = medicineId,
            image = DrugType.setIcon(form)
        )
    ) else imageList.map { image ->
        Image(
            medicineId = medicineId,
            image = image
        )
    }
}

object DecimalAmountInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().isNotEmpty()) {
            val decimalAmount = asCharSequence().toString().replace(',', '.')

            if (decimalAmount.toDoubleOrNull() != null) {
                replace(0, length, decimalAmount)
            } else {
                revertAllChanges()
            }
        } else {
            delete(0, length)
        }
    }
}

object DecimalAmountOutputTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val transformedText = asCharSequence().toString().replace('.', ',')

        replace(0, length, transformedText)
    }
}

object DaysInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        if (asCharSequence().toString().isNotEmpty()) {
            val newValue = asCharSequence().toString().toIntOrNull()

            if (newValue == null || newValue <= 0) {
                revertAllChanges()
            }
        } else {
            delete(0, length)
        }
    }
}