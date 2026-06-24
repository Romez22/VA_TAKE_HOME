package com.virginactive.shared.domain.profile

import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getProfile(): AppResult<UserProfile>
}
