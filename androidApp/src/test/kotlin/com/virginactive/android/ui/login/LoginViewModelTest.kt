package com.virginactive.android.ui.login

import com.virginactive.shared.domain.auth.AuthRepository
import com.virginactive.shared.domain.auth.AuthState
import com.virginactive.shared.domain.auth.LoginUseCase
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.model.Club
import com.virginactive.shared.domain.model.MembershipTier
import com.virginactive.shared.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthRepository(
        var result: AppResult<UserProfile>,
    ) : AuthRepository {
        var loginCalls = 0
        override suspend fun login(username: String, password: String): AppResult<UserProfile> {
            loginCalls++
            return result
        }

        override suspend fun logout() = Unit
        override suspend fun bootstrap(): AuthState = AuthState.LoggedOut
        override val authState: Flow<AuthState> = emptyFlow()
    }

    private fun useCase(result: AppResult<UserProfile>): Pair<LoginUseCase, FakeAuthRepository> {
        val repo = FakeAuthRepository(result)
        return LoginUseCase(repo) to repo
    }

    private val sampleProfile = UserProfile(
        id = "u1",
        firstName = "Avid",
        lastName = "Member",
        email = "avid@example.com",
        membershipTier = MembershipTier.PREMIUM,
        homeClub = Club(id = "club_sea_point", name = "Sea Point"),
    )

    @Test
    fun `valid credentials invoke onLoggedIn exactly once`() = runTest(testDispatcher) {
        val (login, _) = useCase(AppResult.Ok(sampleProfile))
        val vm = LoginViewModel(login)
        var loggedIn = 0

        vm.onEmailChange("avid@example.com")
        vm.onPasswordChange("secret")
        vm.submit { loggedIn++ }
        advanceUntilIdle()

        assertEquals(1, loggedIn)
        assertNull(vm.state.error)
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun `unauthorized error surfaces in state and does not log in`() = runTest(testDispatcher) {
        val (login, _) = useCase(AppResult.Err(DomainError.Unauthorized))
        val vm = LoginViewModel(login)
        var loggedIn = 0

        vm.submit { loggedIn++ }
        advanceUntilIdle()

        assertEquals(0, loggedIn)
        assertEquals(DomainError.Unauthorized, vm.state.error)
        assertFalse(vm.state.isLoading)
    }

    @Test
    fun `validation and rate-limited errors surface distinctly`() = runTest(testDispatcher) {
        val (vLogin, _) = useCase(AppResult.Err(DomainError.Validation("InvalidEmail")))
        val vVm = LoginViewModel(vLogin)
        vVm.submit { }
        advanceUntilIdle()
        assertTrue(vVm.state.error is DomainError.Validation)

        val (rLogin, _) = useCase(AppResult.Err(DomainError.RateLimited("5")))
        val rVm = LoginViewModel(rLogin)
        rVm.submit { }
        advanceUntilIdle()
        assertTrue(rVm.state.error is DomainError.RateLimited)
    }

    @Test
    fun `re-submit while loading is ignored`() = runTest(testDispatcher) {
        val (login, repo) = useCase(AppResult.Ok(sampleProfile))
        val vm = LoginViewModel(login)

        vm.submit { }
        assertTrue(vm.state.isLoading)
        vm.submit { }
        advanceUntilIdle()

        assertEquals(1, repo.loginCalls)
    }
}
