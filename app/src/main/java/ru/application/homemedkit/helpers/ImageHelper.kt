package ru.application.homemedkit.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import ru.application.homemedkit.R

@SuppressLint("Recycle")
fun getIconType(context: Context, form: String): Drawable {
    val icons = context.resources.obtainTypedArray(R.array.medicine_types_icons)
    val types = context.resources.getStringArray(R.array.medicine_types)
    val type = formName(form).uppercase()

    return when (val index = types.indexOf(type)) {
        -1 -> AppCompatResources.getDrawable(context, R.drawable.vector_type_unknown)!!
        else -> icons.getDrawable(index)!!
    }
}