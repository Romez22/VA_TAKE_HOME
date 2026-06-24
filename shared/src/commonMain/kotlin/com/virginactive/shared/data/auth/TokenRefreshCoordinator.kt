package com.virginactive.shared.data.auth

import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.domain.store.TokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class TokenRefreshCoordinator(
    private val api: AuthApi,
    private val store: TokenStore,
) {

    private val mutex = Mutex()

    suspend fun refresh(triggeringAccessToken: String?): Session? = mutex.withLock {
        val current = store.session() ?: return@withLock null

        if (current.accessToken != triggeringAccessToken) return@withLock current

        when (val result = api.refresh(current.refreshToken)) {
            is AppResult.Ok -> result.value.also { store.save(it) }
            is AppResult.Err -> {
                store.clear()
                null
            }
        }
    }
}
