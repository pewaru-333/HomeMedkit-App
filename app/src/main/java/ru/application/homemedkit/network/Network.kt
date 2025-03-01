package ru.application.homemedkit.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import ru.application.homemedkit.helpers.BLANK
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

    suspend fun getImage(dir: File, urls: List<String>?) = if (urls.isNullOrEmpty()) BLANK
    else try {
        val url = urls.first()
        val name = url.substringAfterLast("/").substringBefore(".")

        ktor.prepareRequest { url(url) }
            .execute { it.bodyAsChannel().copyAndClose(File(dir, name).writeChannel()) }

        name
    } catch (e: Throwable) {
        BLANK
    }
}
