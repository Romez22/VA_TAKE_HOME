package com.virginactive.shared.data.profile

import com.virginactive.shared.data.auth.dto.UserProfileDto
import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.DomainErrorException
import com.virginactive.shared.data.remote.mapHttpError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

internal class ProfileApi(
    private val client: HttpClient,
) {

    suspend fun getProfile(): UserProfileDto {
        val response: HttpResponse = client.get("/me")
        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw DomainErrorException(
                mapHttpError(response.status, errorBody, response.headers[HttpHeaders.RetryAfter]),
            )
        }
        return response.body()
    }
}
