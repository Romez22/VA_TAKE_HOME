package com.virginactive.shared.domain.auth

import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): AppResult<UserProfile> =
        repository.login(username, password)
}
