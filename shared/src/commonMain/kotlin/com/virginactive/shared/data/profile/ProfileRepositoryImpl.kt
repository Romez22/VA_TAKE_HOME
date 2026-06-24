package com.virginactive.shared.data.profile

import com.virginactive.shared.data.auth.dto.toDomain
import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.model.UserProfile
import com.virginactive.shared.domain.profile.ProfileRepository

internal class ProfileRepositoryImpl(
    private val profileApi: ProfileApi,
) : ProfileRepository {

    override suspend fun getProfile(): AppResult<UserProfile> =
        safeCall { profileApi.getProfile().toDomain() }
}
