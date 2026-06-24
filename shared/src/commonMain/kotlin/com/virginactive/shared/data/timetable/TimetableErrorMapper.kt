package com.virginactive.shared.data.timetable

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.parseRetryAfter
import com.virginactive.shared.domain.error.DomainError
import io.ktor.http.HttpStatusCode

internal fun mapTimetableError(
    status: HttpStatusCode,
    body: ApiErrorDto?,
    retryAfterHeader: String? = null,
): DomainError =
    when (status.value) {
        400 -> DomainError.Validation(body?.error ?: body?.code)
        401 -> DomainError.Unauthorized
        404 -> DomainError.NotFound(body?.error ?: body?.code)
        409 -> DomainError.Conflict(body?.error ?: body?.code)
        422 -> DomainError.ClassInPast
        429 -> DomainError.RateLimited(retryAfter = parseRetryAfter(retryAfterHeader))
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown("HTTP ${status.value}")
    }
