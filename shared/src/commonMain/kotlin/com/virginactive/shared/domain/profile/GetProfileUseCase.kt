package com.virginactive.shared.domain.profile

import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile

class GetProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): AppResult<UserProfile> =
        repository.getProfile()
}
