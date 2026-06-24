package com.virginactive.shared.domain.home

import com.virginactive.shared.domain.error.AppResult

interface HomeRepository {

    suspend fun getManifest(clubId: String): AppResult<HomeManifest>
}
