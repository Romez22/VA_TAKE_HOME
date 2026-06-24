package com.virginactive.shared.data.auth

import com.virginactive.shared.data.auth.dto.LoginRequestDto
import com.virginactive.shared.data.auth.dto.toDomain
import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.domain.auth.AuthRepository
import com.virginactive.shared.domain.auth.AuthState
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile
import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.domain.store.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val store: TokenStore,
    private val coordinator: TokenRefreshCoordinator,
    private val resetBearerToken: () -> Unit = {},
) : AuthRepository {

    private val authStateFlow = MutableSharedFlow<AuthState>(replay = 1)

    override val authState: Flow<AuthState> = authStateFlow.asSharedFlow()

    override suspend fun login(username: String, password: String): AppResult<UserProfile> =
        safeCall {
            val response = authApi.login(LoginRequestDto(username, password))
            store.save(Session(response.accessToken, response.refreshToken))
            val profile = response.user.toDomain()
            authStateFlow.emit(AuthState.Authenticated(profile))
            profile
        }

    override suspend fun bootstrap(): AuthState =
        if (store.session() != null) {
            AuthState.Authenticated(profile = null)
        } else {
            AuthState.LoggedOut
        }

    override suspend fun logout() {
        store.clear()
        resetBearerToken()
        authStateFlow.emit(AuthState.LoggedOut)
    }

    suspend fun forceLogout() {
        resetBearerToken()
        authStateFlow.emit(AuthState.LoggedOut)
    }
}
