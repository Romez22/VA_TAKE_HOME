package com.virginactive.shared.data.auth

import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.data.remote.createHttpClient
import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.fake.InMemoryTokenStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenRefreshCoordinatorTest {

    private val config = ApiConfig(baseUrl = "http://test.local")

    private fun authApi(engine: io.ktor.client.engine.mock.MockEngine): AuthApi {
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        return AuthApi(client)
    }

    @Test
    fun firstRefreshRotatesAndPersistsBeforeReturning() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val coordinator = TokenRefreshCoordinator(authApi(scriptedAuthEngine(counters)), store)

        val result = coordinator.refresh("acc1")

        assertEquals(1, counters.refreshCalls, "exactly one /auth/refresh")
        assertEquals(Session("acc2", "ref2"), result, "returns the rotated session")
        assertEquals(Session("acc2", "ref2"), store.stored, "rotated pair persisted first")
    }

    @Test
    fun concurrentRefreshCoalescesToSingleFlight() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val coordinator = TokenRefreshCoordinator(authApi(scriptedAuthEngine(counters)), store)

        val results = listOf(
            async { coordinator.refresh("acc1") },
            async { coordinator.refresh("acc1") },
            async { coordinator.refresh("acc1") },
        ).awaitAll()

        assertEquals(1, counters.refreshCalls, "single-flight: exactly ONE /auth/refresh")
        assertEquals(1, store.saveCount, "single-flight persists exactly once")
        assertTrue(results.all { it == Session("acc2", "ref2") }, "all three see the same rotated session")
    }

    @Test
    fun twoConsecutiveRotationsUseTheRotatedRefreshToken() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val engine = io.ktor.client.engine.mock.MockEngine { request ->
            when {
                request.url.encodedPath == "/auth/refresh" -> {
                    counters.refreshCalls++
                    val sent = request.bodyText()
                    counters.refreshRequestBodies += sent
                    val next = if (sent.contains("ref1")) {
                        refreshResponseJson("acc2", "ref2")
                    } else {
                        refreshResponseJson("acc3", "ref3")
                    }
                    respondJson(next)
                }
                else -> respondJson("""{"ok":true}""")
            }
        }
        val coordinator = TokenRefreshCoordinator(authApi(engine), store)

        val first = coordinator.refresh("acc1")
        assertEquals(Session("acc2", "ref2"), first)

        val second = coordinator.refresh("acc2")
        assertEquals(Session("acc3", "ref3"), second)

        assertEquals(2, counters.refreshCalls)
        assertTrue(
            counters.refreshRequestBodies[1].contains("ref2"),
            "second refresh must carry the rotated refresh token, not the original ref1",
        )
        assertTrue(
            !counters.refreshRequestBodies[1].contains("ref1"),
            "second refresh must NOT reuse the invalidated original token",
        )
    }

    @Test
    fun unrecoverableRefreshReturnsNullAndClearsStore() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val coordinator = TokenRefreshCoordinator(
            authApi(scriptedAuthEngine(counters, refreshFails = true)),
            store,
        )

        val result = coordinator.refresh("acc1")

        assertNull(result, "unrecoverable refresh returns null")
        assertNull(store.session(), "store cleared on unrecoverable refresh (forceLogout precondition)")
        assertEquals(1, store.clearCount, "store.clear() invoked exactly once")
    }
}
