package com.virginactive.android.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.home.GetHomeManifestUseCase
import com.virginactive.shared.domain.home.HomeManifest
import com.virginactive.shared.domain.profile.GetProfileUseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getManifest: GetHomeManifestUseCase,
    private val getProfile: GetProfileUseCase,
) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    fun load() {
        if (state.isLoading) return
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val clubId = when (val profile = getProfile()) {
                is AppResult.Ok -> profile.value.homeClub.id
                is AppResult.Err -> ""
            }
            when (val result = getManifest(clubId)) {
                is AppResult.Ok -> state = state.copy(isLoading = false, manifest = result.value)
                is AppResult.Err -> state = state.copy(isLoading = false, error = result.error)
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val manifest: HomeManifest? = null,
)
