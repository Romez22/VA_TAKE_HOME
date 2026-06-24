package com.virginactive.shared.data.timetable

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.DomainErrorException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess

internal class TimetableApi(
    private val client: HttpClient,
) {

    suspend fun getWeek(clubId: String, date: String): String {
        val response: HttpResponse = client.get {
            url { appendPathSegments("clubs", clubId, "classes", "timetable") }
            parameter("date", date)
        }
        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw DomainErrorException(
                mapTimetableError(response.status, errorBody, response.headers[HttpHeaders.RetryAfter]),
            )
        }
        return response.bodyAsText()
    }
}
