package com.virginactive.shared.domain.error

sealed interface DomainError {
    data object Network : DomainError

    data object Timeout : DomainError

    data object Unauthorized : DomainError

    data class Validation(val code: String?) : DomainError

    data class NotFound(val code: String?) : DomainError

    data class Conflict(val code: String?) : DomainError

    data object ClassInPast : DomainError

    data class RateLimited(val retryAfter: String?) : DomainError

    data object Server : DomainError

    data class Serialization(val raw: String?) : DomainError

    data class Unknown(val message: String?) : DomainError
}
