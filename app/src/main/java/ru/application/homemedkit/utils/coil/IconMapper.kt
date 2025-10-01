package ru.application.homemedkit.utils.coil

import android.content.Context
import coil3.map.Mapper
import coil3.request.Options
import ru.application.homemedkit.utils.enums.DrugType
import java.io.File

class IconMapper(private val context: Context) : Mapper<String, Any> {
    override fun map(data: String, options: Options) =
        DrugType.iconMap[data] ?: File(context.filesDir, data)
}