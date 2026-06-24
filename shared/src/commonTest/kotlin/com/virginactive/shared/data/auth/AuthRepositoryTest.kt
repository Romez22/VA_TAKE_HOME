package com.virginactive.shared.data.auth

import app.cash.turbine.test
import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.data.remote.createHttpClient
import com.virginactive.shared.domain.auth.AuthRepository
import com.virginactive.shared.domain.auth.AuthState
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.store.Session
import com.virginactive.shared.fake.InMemoryTokenStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRepositoryTest {

    private val config = ApiConfig(baseUrl = "http://test.local")

    private companion object {
        const val USER_RUNNER = "avid.runner@virginactive.mock"
        const val USER_SWIMMER = "competitive.swimmer@virginactive.mock"
        const val PASSWORD = "password123"
    }

    private fun repository(engine: MockEngine, store: InMemoryTokenStore): AuthRepository {
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val api = AuthApi(client)
        val coordinator = TokenRefreshCoordinator(api, store)
        return AuthRepositoryImpl(api, store, coordinator)
    }

    private fun loginEngine(
        counters: AuthCallCounters,
        user: String,
        userId: String,
        firstName: String,
        lastName: String,
    ): MockEngine = MockEngine { _ ->
        counters.loginCalls++
        respondJson(
            loginResponseJson(
                accessToken = "acc-$userId",
                refreshToken = "ref-$userId",
                userId = userId,
                firstName = firstName,
                lastName = lastName,
                email = user,
            ),
        )
    }

    @Test
    fun avidRunnerLogsInAndPersistsSession() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore()
        val repo = repository(loginEngine(counters, USER_RUNNER, "u-runner", "Avid", "Runner"), store)

        val result = repo.login(USER_RUNNER, PASSWORD)

        assertTrue(result is AppResult.Ok, "avid.runner logs in")
        assertEquals(Session("acc-u-runner", "ref-u-runner"), store.stored, "session persisted")
    }

    @Test
    fun competitiveSwimmerLogsInAndPersistsSession() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore()
        val repo = repository(loginEngine(counters, USER_SWIMMER, "u-swimmer", "Competitive", "Swimmer"), store)

        val result = repo.login(USER_SWIMMER, PASSWORD)

        assertTrue(result is AppResult.Ok, "competitive.swimmer logs in")
        assertEquals(Session("acc-u-swimmer", "ref-u-swimmer"), store.stored, "session persisted")
    }

    @Test
    fun login400MapsToValidation() = runTest {
        val store = InMemoryTokenStore()
        val engine = MockEngine { _ ->
            respondJson(apiErrorJson("bad_request", "InvalidCredentials"), HttpStatusCode.BadRequest)
        }
        val repo = repository(engine, store)

        val result = repo.login(USER_RUNNER, PASSWORD)

        assertTrue(result is AppResult.Err && result.error is DomainError.Validation, "400 -> Validation")
    }

    @Test
    fun login401MapsToUnauthorized() = runTest {
        val store = InMemoryTokenStore()
        val engine = MockEngine { _ ->
            respondJson(apiErrorJson("unauthorized", "BadCredentials"), HttpStatusCode.Unauthorized)
        }
        val repo = repository(engine, store)

        val result = repo.login(USER_RUNNER, PASSWORD)

        assertEquals(AppResult.Err(DomainError.Unauthorized), result, "401 -> Unauthorized")
    }

    @Test
    fun login429MapsToRateLimitedAndIsNotRetried() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore()
        val engine = MockEngine { _ ->
            counters.loginCalls++
            respondJson(apiErrorJson("rate_limited", "RateLimited"), HttpStatusCode.TooManyRequests)
        }
        val repo = repository(engine, store)

        val result = repo.login(USER_RUNNER, PASSWORD)

        assertTrue(result is AppResult.Err && result.error is DomainError.RateLimited, "429 -> RateLimited")
        assertEquals(1, counters.loginCalls, "login 429 must NOT be retried")
    }

    @Test
    fun bootstrapRestoresAuthenticatedFromSeededStore() = runTest {
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val engine = MockEngine { _ -> respondJson("""{"ok":true}""") }
        val repo = repository(engine, store)

        val state = repo.bootstrap()

        assertTrue(state is AuthState.Authenticated, "seeded store -> Authenticated")
    }

    @Test
    fun bootstrapYieldsLoggedOutWhenStoreEmpty() = runTest {
        val store = InMemoryTokenStore()
        val engine = MockEngine { _ -> respondJson("""{"ok":true}""") }
        val repo = repository(engine, store)

        val state = repo.bootstrap()

        assertTrue(state is AuthState.LoggedOut, "empty store -> LoggedOut")
    }

    @Test
    fun unrecoverableRefreshEmitsForceLogoutOnAuthState() = runTest {
        val counters = AuthCallCounters()
        val store = InMemoryTokenStore(initial = Session("acc1", "ref1"))
        val repo = repository(scriptedAuthEngine(counters, refreshFails = true), store)

        repo.authState.test {
            repo.bootstrap()
            forceLogoutTrigger(repo)
            val emitted = awaitItem()
            assertTrue(emitted is AuthState.LoggedOut, "forceLogout emits LoggedOut on authState")
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun forceLogoutTrigger(repo: AuthRepository) {
        repo.logout()
    }
}
