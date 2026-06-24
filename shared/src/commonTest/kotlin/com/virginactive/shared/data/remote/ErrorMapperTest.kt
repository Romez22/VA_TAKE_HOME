package com.virginactive.shared.data.remote

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ErrorMapperTest {

    private fun dto(code: String?) = ApiErrorDto(
        error = "error",
        message = "message",
        code = code,
        requestId = "req-123",
    )

    @Test
    fun mapsBadRequestToValidationWithCode() {
        assertEquals(
            DomainError.Validation("InvalidDate"),
            mapHttpError(HttpStatusCode.BadRequest, dto("InvalidDate")),
        )
    }

    @Test
    fun mapsUnauthorized() {
        assertEquals(DomainError.Unauthorized, mapHttpError(HttpStatusCode.Unauthorized, null))
    }

    @Test
    fun mapsNotFoundWithCode() {
        assertEquals(
            DomainError.NotFound("ClassNotFound"),
            mapHttpError(HttpStatusCode.NotFound, dto("ClassNotFound")),
        )
    }

    @Test
    fun mapsConflictWithCode() {
        assertEquals(
            DomainError.Conflict("AlreadyBooked"),
            mapHttpError(HttpStatusCode.Conflict, dto("AlreadyBooked")),
        )
    }

    @Test
    fun mapsUnprocessableEntityToClassInPast() {
        assertEquals(
            DomainError.ClassInPast,
            mapHttpError(HttpStatusCode.UnprocessableEntity, dto("ClassInPast")),
        )
    }

    @Test
    fun mapsTooManyRequestsToRateLimited() {
        assertEquals(
            DomainError.RateLimited(null),
            mapHttpError(HttpStatusCode.TooManyRequests, null),
        )
    }

    @Test
    fun mapsServerErrorRange() {
        assertEquals(DomainError.Server, mapHttpError(HttpStatusCode.InternalServerError, null))
        assertEquals(DomainError.Server, mapHttpError(HttpStatusCode.BadGateway, null))
    }

    @Test
    fun mapsUnmappedStatusToUnknown() {
        val result = mapHttpError(HttpStatusCode.PaymentRequired, null)
        assertTrue(result is DomainError.Unknown)
    }

    @Test
    fun safeCallReturnsOkOnSuccess() = runTest {
        val result = safeCall { 42 }
        assertEquals(AppResult.Ok(42), result)
    }

    @Test
    fun safeCallMapsTimeout() = runTest {
        val result = safeCall { throw HttpRequestTimeoutException("url", 1000L) }
        assertEquals(AppResult.Err(DomainError.Timeout), result)
    }

    @Test
    fun safeCallMapsSocketTimeoutToTimeout() = runTest {
        val result = safeCall { throw SocketTimeoutException("socket timed out") }
        assertEquals(AppResult.Err(DomainError.Timeout), result)
    }

    @Test
    fun safeCallMapsIOExceptionToNetwork() = runTest {
        val result = safeCall { throw IOException("connection refused") }
        assertEquals(AppResult.Err(DomainError.Network), result)
    }

    @Test
    fun safeCallMapsSerializationException() = runTest {
        val result: AppResult<Int> = safeCall { throw SerializationException("bad json") }
        assertTrue(result is AppResult.Err && result.error is DomainError.Serialization)
    }

    @Test
    fun safeCallRethrowsCancellation() = runTest {
        assertFailsWith<CancellationException> {
            safeCall { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun safeCallMapsUnexpectedToUnknown() = runTest {
        val result: AppResult<Int> = safeCall { throw IllegalStateException("boom") }
        assertTrue(result is AppResult.Err && result.error is DomainError.Unknown)
    }

    @Test
    fun safeCallUnwrapsDomainErrorException() = runTest {
        val result: AppResult<Int> = safeCall {
            throw DomainErrorException(DomainError.Conflict("AlreadyBooked"))
        }
        assertEquals(AppResult.Err(DomainError.Conflict("AlreadyBooked")), result)
    }
}
