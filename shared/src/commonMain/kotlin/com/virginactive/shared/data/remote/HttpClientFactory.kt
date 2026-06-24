package com.virginactive.shared.data.remote

import com.virginactive.shared.data.auth.TokenRefreshCoordinator
import com.virginactive.shared.domain.store.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createHttpClient(
    engine: HttpClientEngine,
    config: ApiConfig,
    json: Json = appJson,
    enableLogging: Boolean = true,
    retryEnabled: Boolean = false,
    installAuth: Boolean = false,
    store: TokenStore? = null,
    coordinator: TokenRefreshCoordinator? = null,
): HttpClient = HttpClient(engine) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(json)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 30_000
    }

    if (enableLogging) {
        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { name -> name.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }
    }

    if (retryEnabled) {
        installRetry()
    }

    if (installAuth && store != null && coordinator != null) {
        install(Auth) {
            bearer {
                loadTokens {
                    store.session()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    coordinator.refresh(oldTokens?.accessToken)
                        ?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                sendWithoutRequest { req ->
                    req.url.encodedPathSegments.firstOrNull { it.isNotEmpty() } != "auth"
                }
            }
        }
    }

    defaultRequest {
        url(config.baseUrl)
        contentType(ContentType.Application.Json)
    }
}

internal fun createAuthlessHttpClient(
    engine: HttpClientEngine,
    config: ApiConfig,
    json: Json = appJson,
    enableLogging: Boolean = true,
): HttpClient = createHttpClient(
    engine = engine,
    config = config,
    json = json,
    enableLogging = enableLogging,
    retryEnabled = false,
    installAuth = false,
)

internal fun resetBearerToken(client: HttpClient) {
    client.authProvider<BearerAuthProvider>()?.clearToken()
}
