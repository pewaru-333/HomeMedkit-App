package ru.application.homemedkit.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import ru.application.homemedkit.R
import ru.application.homemedkit.utils.enums.DrugType
import java.io.File

@Composable
fun MedicineImage(image: String, modifier: Modifier = Modifier, editable: Boolean = false) {
    val context = LocalContext.current

    val model = remember(image, context) {
        DrugType.getIcon(image) ?: File(context.filesDir, image)
    }

    AsyncImage(
        model = model,
        modifier = modifier,
        contentDescription = null,
        error = painterResource(R.drawable.vector_type_unknown),
        alpha = if (editable) 0.4f else 1f
    )
}