package ru.application.homemedkit.utils.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.application.homemedkit.utils.BLANK
import java.io.File
import java.security.MessageDigest

suspend fun File.md5(): String = withContext(Dispatchers.IO) {
    val md = MessageDigest.getInstance("MD5")

    inputStream().use { inputStream ->
        val buffer = ByteArray(8192)
        var bytesRead = inputStream.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        }

        md.digest().joinToString(BLANK) { "%02x".format(it) }
    }
}