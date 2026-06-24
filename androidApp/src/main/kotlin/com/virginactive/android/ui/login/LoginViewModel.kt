package com.virginactive.android.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virginactive.shared.domain.auth.LoginUseCase
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
) : ViewModel() {

    var state by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(email: String) {
        state = state.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        state = state.copy(password = password)
    }

    fun submit(onLoggedIn: () -> Unit) {
        if (state.isLoading) return
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = login(state.email, state.password)) {
                is AppResult.Ok -> {
                    state = state.copy(isLoading = false)
                    onLoggedIn()
                }
                is AppResult.Err -> {
                    state = state.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: DomainError? = null,
)
