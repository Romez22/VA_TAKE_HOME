package com.virginactive.shared.data.remote

import com.virginactive.shared.data.dto.ApiErrorDto
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpClientTest {

    @Serializable
    private data class ClubSummary(val id: String, val name: String)

    private val config = ApiConfig(baseUrl = "http://test.local")

    private fun jsonHeaders() =
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun decodesOkBodyToTypedModel() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"id":"club-1","name":"Virgin Active Sandton"}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders(),
            )
        }
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val result = safeCall {
            val response: HttpResponse = client.get("/clubs/club-1")
            response.body<ClubSummary>()
        }
        assertEquals(AppResult.Ok(ClubSummary("club-1", "Virgin Active Sandton")), result)
    }

    @Test
    fun mapsNotFoundApiErrorBodyToDomainNotFound() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"error":"not_found","message":"Class not found","code":"ClassNotFound","requestId":"r-1"}""",
                status = HttpStatusCode.NotFound,
                headers = jsonHeaders(),
            )
        }
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)

        val result: AppResult<ClubSummary> = safeCall {
            val response: HttpResponse = client.get("/clubs/missing")
            if (response.status.value in 200..299) {
                response.body<ClubSummary>()
            } else {
                val errorBody = runCatching { response.body<ApiErrorDto>() }.getOrNull()
                throw DomainErrorException(mapHttpError(response.status, errorBody))
            }
        }
        assertTrue(result is AppResult.Err)
        assertEquals(DomainError.NotFound("ClassNotFound"), (result as AppResult.Err).error)
    }

    @Test
    fun requestTimeoutSurfacesAsDomainTimeout() = runTest {
        val engine = MockEngine { _ ->
            delay(60_000)
            respond(content = "{}", status = HttpStatusCode.OK, headers = jsonHeaders())
        }
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val result = safeCall {
            val response: HttpResponse = client.get("/slow")
            response.body<ClubSummary>()
        }
        assertEquals(AppResult.Err(DomainError.Timeout), result)
    }
}
