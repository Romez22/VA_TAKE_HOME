package com.virginactive.shared.data.booking

import com.virginactive.shared.data.booking.dto.BookingResponseDto
import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.DomainErrorException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess

internal class BookingApi(
    private val client: HttpClient,
) {

    suspend fun book(clubId: String, classId: String): BookingResult {
        val response: HttpResponse = client.post {
            url { appendEncodedPathSegments(bookingPathSegments(clubId, classId)) }
        }
        if (response.status.isSuccess()) {
            val dto = response.body<BookingResponseDto>()
            return if (response.status == HttpStatusCode.Accepted) {
                BookingResult.Waitlisted(dto)
            } else {
                BookingResult.Confirmed(dto)
            }
        }
        val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
        if (response.status == HttpStatusCode.Conflict) {
            when (errorBody?.error) {
                "AlreadyBooked" -> return BookingResult.Already(wasWaitlist = false)
                "AlreadyWaitlisted" -> return BookingResult.Already(wasWaitlist = true)
            }
        }
        throw DomainErrorException(
            mapBookingError(response.status, errorBody, response.headers[HttpHeaders.RetryAfter]),
        )
    }

    suspend fun cancel(clubId: String, classId: String) {
        val response: HttpResponse = client.delete {
            url { appendEncodedPathSegments(bookingPathSegments(clubId, classId)) }
        }
        if (response.status.isSuccess()) return
        val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
        throw DomainErrorException(
            mapBookingError(response.status, errorBody, response.headers[HttpHeaders.RetryAfter]),
        )
    }
}

private fun bookingPathSegments(clubId: String, classId: String): List<String> =
    listOf("clubs", clubId, "classes", classId, "bookings")
        .map { it.encodeURLPathPart().replace(":", "%3A") }

internal sealed interface BookingResult {
    data class Confirmed(val dto: BookingResponseDto) : BookingResult
    data class Waitlisted(val dto: BookingResponseDto) : BookingResult
    data class Already(val wasWaitlist: Boolean) : BookingResult
}
