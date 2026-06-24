package com.virginactive.shared.data.home

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.DomainErrorException
import com.virginactive.shared.data.remote.mapHttpError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

internal class HomeApi(
    private val client: HttpClient,
) {

    suspend fun getManifest(): String {
        val response: HttpResponse = client.get("/home/manifest")
        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw DomainErrorException(
                mapHttpError(response.status, errorBody, response.headers[HttpHeaders.RetryAfter]),
            )
        }
        return response.bodyAsText()
    }
}
