package ru.application.homemedkit.network

import android.net.Uri
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.util.encodeBase64
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.network.auth.PKCEUtils
import ru.application.homemedkit.network.auth.yandex
import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.network.models.auth.Disk
import ru.application.homemedkit.network.models.auth.FileMetadata
import ru.application.homemedkit.network.models.auth.Token
import ru.application.homemedkit.network.models.auth.UploadLink
import ru.application.homemedkit.utils.AUTH_URL_YANDEX
import ru.application.homemedkit.utils.CLIENT_ID_YANDEX
import ru.application.homemedkit.utils.CLIENT_SECRET_YANDEX
import ru.application.homemedkit.utils.TOKEN_URL_YANDEX
import ru.application.homemedkit.utils.di.Preferences
import java.io.File

object Network {
    private val defaultClient = HttpClient(Android) {
        engine {
            dispatcher = Dispatchers.IO
        }

        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    private val medicineClient = defaultClient.config {
        defaultRequest { url("https://mobile.api.crpt.ru/") }
    }

    private val yandexClient = defaultClient.config {
        defaultRequest {
            url("https://cloud-api.yandex.net/v1/disk/")
        }

        install(Auth) {
            yandex {
                sendWithoutRequest = { request ->
                    request.url.host == "oauth.yandex.ru"
                }

                loadTokens {
                    Preferences.token?.let {
                        BearerTokens(it.accessToken, it.refreshToken)
                    }
                }

                refreshTokens {
                    oldTokens?.refreshToken?.let { refreshToken ->
                        val newToken = Yandex.refreshToken(refreshToken) {
                            markAsRefreshTokenRequest()
                        }

                        newToken?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }
                }
            }
        }
    }

    suspend fun getMedicine(code: String) = try {
        val codeType = if (code.length == 13) "ean13" else "datamatrix"
        val response = medicineClient.get("mobile/check") {
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

    suspend fun getImage(dir: File, urls: List<String>) = withContext(Dispatchers.IO) {
        supervisorScope {
            val requests = urls.map { url ->
                async(Dispatchers.IO.limitedParallelism(3)) {
                    try {
                        val response = defaultClient.get(url)

                        if (response.status == HttpStatusCode.OK) {
                            Url(url).segments.lastOrNull()?.let { name ->
                                val file = File(dir, name)

                                response.bodyAsChannel().copyAndClose(file.writeChannel())

                                name
                            }
                        } else {
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            }

            requests.mapNotNull { it.await() }
        }
    }

    internal object Yandex {
        private val mutex by lazy(::Mutex)
        val authUri: Uri
            get() {
                val verifier = PKCEUtils.generateCodeVerifier()
                val challenge = PKCEUtils.generateCodeChallenger(verifier)

                Preferences.saveCodeVerifier(verifier)

                return AUTH_URL_YANDEX.toUri()
                    .buildUpon()
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("client_id", CLIENT_ID_YANDEX)
                    .appendQueryParameter("force_confirm", "yes")
                    .appendQueryParameter("code_challenge", challenge)
                    .appendQueryParameter("code_challenge_method", "S256")
                    .build()
            }

        suspend fun getToken(code: String): Token? {
            val verifier = Preferences.codeVerifier ?: return null
            val encoded = "$CLIENT_ID_YANDEX:$CLIENT_SECRET_YANDEX".encodeBase64()

            return try {
                defaultClient.submitForm(
                    url = TOKEN_URL_YANDEX,
                    block = { header(HttpHeaders.Authorization, "Basic $encoded") },
                    formParameters = parameters {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("code_verifier", verifier)
                    }
                ).body<Token>()
            } catch (_: Exception) {
                null
            } finally {
                Preferences.removeCodeVerifier()
            }
        }

        suspend fun refreshToken(refreshToken: String, builder: HttpRequestBuilder.() -> Unit): Token? = mutex.withLock {
            with(Preferences.token) {
                if (this != null && this.refreshToken != refreshToken) {
                    return@withLock this
                }
            }

            val encoded = "$CLIENT_ID_YANDEX:$CLIENT_SECRET_YANDEX".encodeBase64()

            repeat(3) { attempt ->
                try {
                    val response = defaultClient.submitForm(
                        url = TOKEN_URL_YANDEX,
                        block = {
                            builder()
                            header(HttpHeaders.Authorization, "Basic $encoded")
                        },
                        formParameters = parameters {
                            append("grant_type", "refresh_token")
                            append("refresh_token", refreshToken)
                        }
                    )

                    if (response.status.isSuccess()) {
                        val newToken = response.body<Token>()
                        Preferences.saveToken(newToken)
                        return@withLock newToken
                    } else if (response.status == HttpStatusCode.BadRequest || response.status == HttpStatusCode.Unauthorized) {
                        Preferences.saveToken(Token.empty)
                        return@withLock null
                    }
                } catch (e: Exception) {
                    if (e !is IOException || attempt == 2) {
                        return@withLock null
                    }

                    delay(1000L * (attempt + 1))
                }
            }

            return@withLock null
        }

        fun clearToken() = yandexClient.authProvider<BearerAuthProvider>()?.clearToken()

        suspend fun checkConnection() = try {
            val request = yandexClient.request { method = HttpMethod.Get }

            request.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }

        suspend fun getAvailableSpace() = try {
            val diskInfo = yandexClient.request { method = HttpMethod.Get }.body<Disk>()

            (diskInfo.totalSpace - diskInfo.usedSpace)
        } catch (_: Exception) {
            -1L
        }

        suspend fun createFolder(path: String) = try {
            val request = yandexClient.put("resources") {
                parameter("path", path)
            }

            request.status == HttpStatusCode.Created
        } catch (_: Exception) {
            false
        }

        suspend fun getFileMetadata(path: String) = try {
            val fileMetadata = yandexClient.get("resources") {
                parameter("path", path)
            }.body<FileMetadata>()

            fileMetadata.mapper()
        } catch (_: Exception) {
            null
        }

        suspend fun getImagesMetadata(path: String = "homemeds/images") = try {
            val folder = yandexClient.get("resources") {
                parameter("path", path)
            }.body<FileMetadata>()

            folder.embedded?.items
        } catch (_: Exception) {
            null
        }

        suspend fun uploadFile(path: String, file: File, overwrite: Boolean = true): Boolean {
            val response = yandexClient.get("resources/upload") {
                parameter("path", path)
                parameter("overwrite", overwrite)
            }

            if (response.status == HttpStatusCode.OK) {
                val uploadLink = response.body<UploadLink>()

                val upload = yandexClient.submitFormWithBinaryData(
                    url = uploadLink.href,
                    formData = formData {
                        append("file", file.readBytes())
                    }
                )

                return if (upload.status == HttpStatusCode.Created) {
                    true
                } else {
                    throw IOException()
                }
            } else {
                throw IOException()
            }
        }

        suspend fun downloadFile(path: String, file: File) = try {
            val response = yandexClient.get("resources/download") {
                parameter("path", path)
            }

            if (response.status == HttpStatusCode.OK) {
                val downloadLink = response.body<UploadLink>()

                val fileResponse = yandexClient.get(downloadLink.href)

                if (fileResponse.status == HttpStatusCode.OK) {
                    file.writeChannel().use {
                        fileResponse.bodyAsChannel().copyAndClose(this)
                    }

                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
