package com.virginactive.shared.data.home

import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.home.HomeManifest
import com.virginactive.shared.domain.home.HomeRepository

internal class HomeRepositoryImpl(
    private val homeApi: HomeApi,
) : HomeRepository {

    override suspend fun getManifest(clubId: String): AppResult<HomeManifest> =
        safeCall { decodeManifest(homeApi.getManifest()) }
}
