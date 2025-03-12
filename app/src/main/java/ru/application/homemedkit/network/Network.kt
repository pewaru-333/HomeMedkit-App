package ru.application.homemedkit.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.application.homemedkit.helpers.CIS
import ru.application.homemedkit.network.models.MainModel
import java.io.File

object Network {
    private val ktor = HttpClient(Android) {
        defaultRequest { url("https://mobile.api.crpt.ru/") }
        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun getMedicine(cis: String) =
        ktor.get("mobile/check") { parameter(CIS, cis) }.body<MainModel>()

    suspend fun getImage(dir: File, urls: List<String>?): List<String> {
        if (urls.isNullOrEmpty())
            return emptyList<String>()

        val images = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            urls.forEach { url ->
                try {
                    val name = url.substringAfterLast("/").substringBefore(".")
                    val file = File(dir, name)
                    val response = ktor.get(url)

                    file.writeChannel().use {
                        response.bodyAsChannel().copyAndClose(this)
                    }

                    images.add(name)
                } catch (_: Throwable) {
                    null
                }
            }
        }

        return images
    }
}
