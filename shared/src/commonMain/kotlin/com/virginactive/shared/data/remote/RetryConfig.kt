package com.virginactive.shared.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.HttpMethod

internal fun HttpClientConfig<*>.installRetry() {
    install(HttpRequestRetry) {
        maxRetries = 2
        retryIf { request, response ->
            val idempotent =
                request.method == HttpMethod.Get || request.method == HttpMethod.Delete
            idempotent && (response.status.value == 429 || response.status.value in 500..599)
        }
        exponentialDelay(base = 2.0, maxDelayMs = 8_000)
    }
}
