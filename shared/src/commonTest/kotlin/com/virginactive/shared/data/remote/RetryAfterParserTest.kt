package com.virginactive.shared.data.remote

import com.virginactive.shared.domain.error.DomainError
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class RetryAfterParserTest {

    @Test
    fun parsesIntegerDeltaSeconds() {
        assertEquals("2", parseRetryAfter("2"))
    }

    @Test
    fun parsesHttpDateWithoutThrowing() {
        val httpDate = "Wed, 21 Oct 2026 07:28:00 GMT"
        assertNotNull(parseRetryAfter(httpDate))
    }

    @Test
    fun returnsNullForNull() {
        assertNull(parseRetryAfter(null))
    }

    @Test
    fun returnsNullForGarbage() {
        assertNull(parseRetryAfter("???not-a-header???"))
    }

    @Test
    fun returnsNullForBlank() {
        assertNull(parseRetryAfter("   "))
    }

    @Test
    fun mapHttpErrorPopulatesRetryAfterFromHeader() {
        assertEquals(
            DomainError.RateLimited("2"),
            mapHttpError(HttpStatusCode.TooManyRequests, null, retryAfterHeader = "2"),
        )
    }

    @Test
    fun mapHttpErrorTwoArgStillYieldsNullRetryAfter() {
        assertEquals(
            DomainError.RateLimited(null),
            mapHttpError(HttpStatusCode.TooManyRequests, null),
        )
    }
}
