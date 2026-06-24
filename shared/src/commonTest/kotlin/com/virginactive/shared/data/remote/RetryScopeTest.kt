package com.virginactive.shared.data.remote

import com.virginactive.shared.data.auth.AuthCallCounters
import com.virginactive.shared.data.auth.retryScriptEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetryScopeTest {

    private val config = ApiConfig(baseUrl = "http://test.local")

    private fun retryingClient(engine: io.ktor.client.engine.mock.MockEngine) =
        createHttpClient(
            engine = engine,
            config = config,
            enableLogging = false,
            retryEnabled = true,
        )

    @Test
    fun getRetriesOn429ThenSucceeds() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.TooManyRequests, HttpStatusCode.OK),
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.get("/clubs")

        assertEquals(HttpStatusCode.OK, response.status, "GET succeeds after one retry")
        assertEquals(2, counter.protectedCalls, "GET 429 retried exactly once (2 attempts)")
    }

    @Test
    fun getRetriesOn500ThenSucceeds() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.InternalServerError, HttpStatusCode.OK),
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.get("/clubs")

        assertEquals(HttpStatusCode.OK, response.status, "GET succeeds after 500-then-200")
        assertEquals(2, counter.protectedCalls)
    }

    @Test
    fun deleteRetriesOn500ThenSucceeds() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.InternalServerError, HttpStatusCode.OK),
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.delete("/bookings/b-1")

        assertEquals(HttpStatusCode.OK, response.status, "DELETE (idempotent) is retried")
        assertEquals(2, counter.protectedCalls)
    }

    @Test
    fun postIsNeverRetried() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.InternalServerError, HttpStatusCode.OK),
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.post("/bookings")

        assertEquals(HttpStatusCode.InternalServerError, response.status, "POST 500 surfaces, not retried")
        assertEquals(1, counter.protectedCalls, "POST attempted exactly once")
    }

    @Test
    fun getHonoursRetryAfterHeaderUnderVirtualTime() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.TooManyRequests, HttpStatusCode.OK),
            retryAfterSeconds = 2,
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.get("/clubs")

        assertEquals(HttpStatusCode.OK, response.status, "Retry-After honoured then success")
        assertEquals(2, counter.protectedCalls)
    }

    @Test
    fun getFallsBackToBackoffWhenNoRetryAfterHeader() = runTest {
        val counter = AuthCallCounters()
        val engine = retryScriptEngine(
            sequence = listOf(HttpStatusCode.TooManyRequests, HttpStatusCode.OK),
            retryAfterSeconds = null,
            counter = counter,
        )
        val client = retryingClient(engine)

        val response: HttpResponse = client.get("/clubs")

        assertTrue(response.status.value in 200..299, "backoff fallback still succeeds")
        assertEquals(2, counter.protectedCalls)
    }
}
