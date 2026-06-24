package com.virginactive.shared.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AppComponentTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun appComponentResolvesEveryUseCase() {
        startKoin {
            modules(testPlatformModule(), networkModule(), coreModule())
        }

        val c = AppComponent()

        assertNotNull(c.loginUseCase)
        assertNotNull(c.logoutUseCase)
        assertNotNull(c.getHomeManifestUseCase)
        assertNotNull(c.getProfileUseCase)
        assertNotNull(c.bookClassUseCase)
        assertNotNull(c.cancelBookingUseCase)
        assertNotNull(c.getTimetableUseCase)
        assertNotNull(c.authRepository)
    }
}
