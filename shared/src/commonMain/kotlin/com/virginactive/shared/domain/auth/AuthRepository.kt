package com.virginactive.shared.domain.auth

import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(username: String, password: String): AppResult<UserProfile>

    suspend fun logout()

    suspend fun bootstrap(): AuthState

    val authState: Flow<AuthState>
}
