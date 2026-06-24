package com.virginactive.shared.data.remote

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal class DomainErrorException(val domainError: DomainError) : Exception()

internal fun mapHttpError(
    status: HttpStatusCode,
    body: ApiErrorDto?,
    retryAfterHeader: String? = null,
): DomainError =
    when (status.value) {
        400 -> DomainError.Validation(body?.code)
        401 -> DomainError.Unauthorized
        404 -> DomainError.NotFound(body?.code)
        409 -> DomainError.Conflict(body?.code)
        422 -> DomainError.ClassInPast
        429 -> DomainError.RateLimited(retryAfter = parseRetryAfter(retryAfterHeader))
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown("HTTP ${status.value}")
    }

internal suspend inline fun <T> safeCall(crossinline block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Ok(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: DomainErrorException) {
        AppResult.Err(e.domainError)
    } catch (e: HttpRequestTimeoutException) {
        AppResult.Err(DomainError.Timeout)
    } catch (e: ConnectTimeoutException) {
        AppResult.Err(DomainError.Timeout)
    } catch (e: SocketTimeoutException) {
        AppResult.Err(DomainError.Timeout)
    } catch (e: SerializationException) {
        AppResult.Err(DomainError.Serialization(e.message))
    } catch (e: IOException) {
        AppResult.Err(DomainError.Network)
    } catch (e: Throwable) {
        AppResult.Err(DomainError.Unknown(e.message))
    }
