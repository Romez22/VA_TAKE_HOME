package com.virginactive.shared.domain.error

sealed interface AppResult<out T> {
    data class Ok<T>(val value: T) : AppResult<T>
    data class Err(val error: DomainError) : AppResult<Nothing>
}
