package com.virginactive.shared.domain.auth

import com.virginactive.shared.domain.model.UserProfile

sealed interface AuthState {
    data class Authenticated(val profile: UserProfile?) : AuthState

    data object LoggedOut : AuthState
}
