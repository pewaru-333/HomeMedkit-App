package ru.application.homemedkit.network

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.utils.extensions.toSHA256
import java.io.File

object Network {
    private val ktor = HttpClient(Android) {
        engine {
            dispatcher = Dispatchers.IO
        }

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

    suspend fun getMedicine(code: String) = try {
        val codeType = if (code.length == 13) "ean13" else "datamatrix"
        val response = ktor.get("mobile/check") {
            parameter("code", code)
            parameter("codeType", codeType)
        }

        if (response.status == HttpStatusCode.OK) {
            val model = response.body<MainModel>()

            if (model.codeFounded) {
                Response.Success(model)
            } else {
                Response.Error.CodeNotFound
            }
        } else {
            Response.Error.UnknownError
        }
    } catch (e: Throwable) {
        when (e) {
            is NoTransformationFoundException -> Response.Error.IncorrectCode
            else -> Response.Error.NetworkError(code)
        }
    }

    suspend fun getImage(dir: File, urls: List<String>): List<String> {
        val requests = withContext(Dispatchers.IO) {
            urls.map { url ->
                async {
                    try {
                        val response = ktor.get(url)

                        if (response.status != HttpStatusCode.OK) null
                        else {
                            val name = url.toSHA256()
                            val file = File(dir, name)

                            file.writeChannel().use {
                                response.bodyAsChannel().copyAndClose(this)
                            }

                            name
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
        }

        return requests.mapNotNull { it.await() }
    }
}
