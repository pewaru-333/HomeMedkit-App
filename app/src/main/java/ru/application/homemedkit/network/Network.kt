package ru.application.homemedkit.network

import android.net.Uri
import androidx.core.net.toUri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.content.ProgressListener
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.application.homemedkit.models.events.Response
import ru.application.homemedkit.network.auth.PKCEUtils
import ru.application.homemedkit.network.auth.yandex
import ru.application.homemedkit.network.models.MainModel
import ru.application.homemedkit.network.models.auth.Disk
import ru.application.homemedkit.network.models.auth.FileMetadata
import ru.application.homemedkit.network.models.auth.Token
import ru.application.homemedkit.network.models.auth.UploadLink
import ru.application.homemedkit.utils.*
import ru.application.homemedkit.utils.di.Preferences
import java.io.File
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.milliseconds

object Network {
    private const val OBFUSCATED_URL = $$"^jjfi0%%ceX_b[$Wf_$Yhfj$hk%"

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
        defaultRequest { url(shiftCipher(OBFUSCATED_URL)) }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000L
            socketTimeoutMillis = 30_000L
            requestTimeoutMillis = 45_000L
        }
    }

    private val imageClient by lazy {
        defaultClient.config {
            install(BodyProgress)
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000L
                socketTimeoutMillis = 30_000L
                requestTimeoutMillis = 30_000L
            }
        }
    }

    private val yandexClient by lazy {
        defaultClient.config {
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
    }

    suspend fun getMedicine(code: String): Response {
        val scanned = Code.parse(code) ?: return Response.Error.IncorrectCode
        return getMedicine(scanned)
    }

    suspend fun getMedicine(scanned: Code) = try {
        val url = shiftCipher(if (scanned.isEan13) "ceX_b[%Y^[Ya" else "l(%ceX_b[%Y^[Ya")
        val response = medicineClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("code", if (scanned.isEan13) scanned.value else "{FNC1}${scanned.value}")
                    put("codeType", if (scanned.isEan13) "ean13" else "datamatrix")
                }
            )
        }

        when (response.status) {
            HttpStatusCode.RequestTimeout, HttpStatusCode.GatewayTimeout -> Response.Error.NetworkError(scanned.value)
            HttpStatusCode.BadRequest -> Response.Error.IncorrectCode
            HttpStatusCode.NotFound -> Response.Error.CodeNotFound
            HttpStatusCode.OK -> {
                val model = response.body<MainModel>()
                if (model.codeFounded) Response.Success(model)
                else Response.Error.CodeNotFound
            }

            else -> Response.Error.UnknownError
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        when (e) {
            is HttpRequestTimeoutException, is IOException, is UnresolvedAddressException -> Response.Error.NetworkError(scanned.value)
            else -> Response.Error.UnknownError
        }
    }

    suspend fun getImage(dir: File, urls: List<String>, progress: IProgress?): List<String> {
        val total = urls.size
        val mutex = Mutex()

        val files = List(total) { File(dir, "${UUID.randomUUID()}_product.jpg") }
        var completed = 0
        var lastPercent = -1

        suspend fun reportBytes(received: Long, length: Long?) {
            if (length == null || length <= 0L) return
            val percent = (received.toDouble() / length * 100).toInt().coerceIn(0, 99)
            mutex.withLock {
                currentCoroutineContext().ensureActive()
                if (completed == 0 && percent > lastPercent) {
                    lastPercent = percent
                    progress?.onProgress(0, total, percent)
                }
            }
        }

        val listener = if (total == 1 && progress != null) ProgressListener(::reportBytes) else null
        return try {
            progress?.onProgress(0, total, null)
            withContext(Dispatchers.IO) {
                urls.mapIndexed { index, url ->
                    async {
                        val file = files[index]
                        val name = try {
                            imageClient
                                .prepareGet(url) { onDownload(listener) }
                                .execute { response ->
                                    if (response.status != HttpStatusCode.OK) return@execute null

                                    val channel = response.bodyAsChannel()
                                    file.outputStream().use { channel.copyTo(it) }

                                    val cause = channel.closedCause
                                    if (cause != null) throw cause
                                    file.name
                                }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            file.delete()
                            null
                        }

                        if (progress != null) {
                            mutex.withLock {
                                currentCoroutineContext().ensureActive()
                                completed++
                                progress.onProgress(completed, total, if (total == 1) 100 else null)
                            }
                        }
                        name
                    }
                }.awaitAll().filterNotNull()
            }
        } catch (e: Throwable) {
            withContext(NonCancellable) {
                withContext(Dispatchers.IO) {
                    files.forEach(File::delete)
                }
            }

            throw e
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
            val encoded = Base64.encode("$CLIENT_ID_YANDEX:$CLIENT_SECRET_YANDEX".encodeToByteArray())

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

            val encoded = Base64.encode("$CLIENT_ID_YANDEX:$CLIENT_SECRET_YANDEX".encodeToByteArray())

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

                    delay((1000L * (attempt + 1)).milliseconds)
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
