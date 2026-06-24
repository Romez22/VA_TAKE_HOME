package com.virginactive.shared.domain.home

import com.virginactive.shared.domain.error.AppResult

class GetHomeManifestUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(clubId: String): AppResult<HomeManifest> =
        repository.getManifest(clubId)
}
