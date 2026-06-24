package com.virginactive.shared.di

import com.virginactive.shared.data.auth.AuthApi
import com.virginactive.shared.data.auth.TokenRefreshCoordinator
import com.virginactive.shared.data.booking.BookingApi
import com.virginactive.shared.data.home.HomeApi
import com.virginactive.shared.data.profile.ProfileApi
import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.domain.auth.AuthRepository
import com.virginactive.shared.domain.auth.LoginUseCase
import com.virginactive.shared.domain.auth.LogoutUseCase
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.BookingRepository
import com.virginactive.shared.domain.booking.CancelBookingUseCase
import com.virginactive.shared.domain.home.GetHomeManifestUseCase
import com.virginactive.shared.domain.home.HomeRepository
import com.virginactive.shared.domain.profile.GetProfileUseCase
import com.virginactive.shared.domain.profile.ProfileRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class KoinGraphTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun networkGraphResolvesAllDefinitions() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        val httpClient: HttpClient = get()
        val json: Json = get()
        val apiConfig: ApiConfig = get()

        assertNotNull(httpClient)
        assertNotNull(json)
        assertNotNull(apiConfig)
        assertEquals("http://test.local", apiConfig.baseUrl)
    }

    @Test
    fun fullAuthGraphResolvesWithNoMissingDefinitions() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        assertNotNull(get<AuthApi>())
        assertNotNull(get<TokenRefreshCoordinator>())
        assertNotNull(get<AuthRepository>())
        assertNotNull(get<LoginUseCase>())
        assertNotNull(get<LogoutUseCase>())
    }

    @Test
    fun fullHomeProfileGraphResolvesWithNoMissingDefinitions() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        assertNotNull(get<HomeApi>())
        assertNotNull(get<HomeRepository>())
        assertNotNull(get<GetHomeManifestUseCase>())
        assertNotNull(get<ProfileApi>())
        assertNotNull(get<ProfileRepository>())
        assertNotNull(get<GetProfileUseCase>())
    }

    @Test
    fun fullBookingGraphResolvesWithNoMissingDefinitions() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        assertNotNull(get<BookingApi>())
        assertNotNull(get<BookingRepository>())
        assertNotNull(get<BookClassUseCase>())
        assertNotNull(get<CancelBookingUseCase>())
    }

    @Test
    fun authlessAndBearerClientsAreDistinctRegistrations() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        val bearerClient: HttpClient = get()
        val authlessClient: HttpClient = get(AuthlessClient)

        assertNotSame(bearerClient, authlessClient)
    }
}
