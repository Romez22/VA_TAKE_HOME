package com.virginactive.shared.data.auth

import com.virginactive.shared.data.auth.dto.LoginRequestDto
import com.virginactive.shared.data.auth.dto.LoginResponseDto
import com.virginactive.shared.data.auth.dto.RefreshRequestDto
import com.virginactive.shared.data.auth.dto.RefreshResponseDto
import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.data.remote.DomainErrorException
import com.virginactive.shared.data.remote.mapHttpError
import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.store.Session
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

internal class AuthApi(
    private val client: HttpClient,
) {

    suspend fun login(request: LoginRequestDto): LoginResponseDto {
        val response: HttpResponse = client.post("/auth/login") { setBody(request) }
        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw DomainErrorException(mapHttpError(response.status, errorBody))
        }
        return response.body()
    }

    suspend fun refresh(refreshToken: String): AppResult<Session> = safeCall {
        val response: HttpResponse =
            client.post("/auth/refresh") { setBody(RefreshRequestDto(refreshToken)) }
        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw DomainErrorException(mapHttpError(response.status, errorBody))
        }
        val dto = response.body<RefreshResponseDto>()
        Session(accessToken = dto.accessToken, refreshToken = dto.refreshToken)
    }
}
