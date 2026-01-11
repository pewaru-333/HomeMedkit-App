package ru.application.homemedkit.network.auth

import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.AuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun AuthConfig.yandex(block: YandexAuthConfig.() -> Unit) {
    val config = YandexAuthConfig().apply(block)
    providers.add(
        YandexAuthProvider(
            refreshTokens = config.refreshTokens,
            loadTokens = config.loadTokens,
            sendWithoutRequestCallback = config.sendWithoutRequest,
            realm = config.realm
        )
    )
}

class YandexAuthConfig {
    var refreshTokens: suspend RefreshTokensParams.() -> BearerTokens? = { null }
    var loadTokens: suspend () -> BearerTokens? = { null }
    var sendWithoutRequest: (HttpRequestBuilder) -> Boolean = { true }
    var realm: String? = null

    fun refreshTokens(block: suspend RefreshTokensParams.() -> BearerTokens?) {
        refreshTokens = block
    }

    fun loadTokens(block: suspend () -> BearerTokens?) {
        loadTokens = block
    }
}

class YandexAuthProvider(
    private val refreshTokens: suspend RefreshTokensParams.() -> BearerTokens?,
    private val loadTokens: suspend () -> BearerTokens?,
    private val sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true },
    private val realm: String?
) : AuthProvider {
    private val mutex = Mutex()
    private var cachedToken: BearerTokens? = null

    @Deprecated("Please use sendWithoutRequest function instead", level = DeprecationLevel.ERROR)
    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    override fun sendWithoutRequest(request: HttpRequestBuilder) = sendWithoutRequestCallback(request)

    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        if (!auth.authScheme.equals("OAuth", ignoreCase = true)) return false
        return realm == null || (auth is HttpAuthHeader.Parameterized && auth.parameter("realm") == realm)
    }

    override suspend fun addRequestHeaders(request: HttpRequestBuilder, authHeader: HttpAuthHeader?) {
        val token = mutex.withLock {
            cachedToken ?: loadTokens().also { cachedToken = it }
        } ?: return

        request.headers {
            val tokenValue = "OAuth ${token.accessToken}"
            if (contains(HttpHeaders.Authorization)) {
                remove(HttpHeaders.Authorization)
            }
            if (!request.attributes.contains(AuthCircuitBreaker)) {
                append(HttpHeaders.Authorization, tokenValue)
            }
        }
    }

    override suspend fun refreshToken(response: HttpResponse): Boolean {
        return mutex.withLock {
            val currentToken = cachedToken

            val params = RefreshTokensParams(response.call.client, response, currentToken)
            val newToken = refreshTokens(params)

            if (newToken != null) {
                cachedToken = newToken
                true
            } else {
                false
            }
        }
    }
}