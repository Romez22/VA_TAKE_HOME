package com.virginactive.shared.di

import com.virginactive.shared.data.auth.AuthApi
import com.virginactive.shared.data.auth.AuthRepositoryImpl
import com.virginactive.shared.data.auth.TokenRefreshCoordinator
import com.virginactive.shared.data.booking.BookingApi
import com.virginactive.shared.data.booking.BookingRepositoryImpl
import com.virginactive.shared.data.home.HomeApi
import com.virginactive.shared.data.home.HomeRepositoryImpl
import com.virginactive.shared.data.profile.ProfileApi
import com.virginactive.shared.data.profile.ProfileRepositoryImpl
import com.virginactive.shared.data.timetable.TimetableApi
import com.virginactive.shared.data.timetable.TimetableRepositoryImpl
import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.data.remote.appJson
import com.virginactive.shared.data.remote.createAuthlessHttpClient
import com.virginactive.shared.data.remote.createHttpClient
import com.virginactive.shared.data.remote.resetBearerToken
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
import com.virginactive.shared.domain.timetable.GetTimetableUseCase
import com.virginactive.shared.domain.timetable.TimetableRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun platformModule(): Module

internal val AuthlessClient = named("authless")

fun networkModule(): Module = module {
    single<Json> { appJson }
    single { ApiConfig(baseUrl = get<PlatformApiHost>().baseUrl) }

    single<HttpClient>(AuthlessClient) {
        createAuthlessHttpClient(engine = get<HttpClientEngine>(), config = get(), json = get())
    }

    single<HttpClient> {
        createHttpClient(
            engine = get<HttpClientEngine>(),
            config = get(),
            json = get(),
            retryEnabled = true,
            installAuth = true,
            store = get(),
            coordinator = get(),
        )
    }
}

fun coreModule(): Module = module {
    single { AuthApi(client = get<HttpClient>(AuthlessClient)) }

    single { TokenRefreshCoordinator(api = get(), store = get()) }

    single<AuthRepository> {
        val bearerClient = get<HttpClient>()
        AuthRepositoryImpl(
            authApi = get(),
            store = get(),
            coordinator = get(),
            resetBearerToken = { resetBearerToken(bearerClient) },
        )
    }

    factory { LoginUseCase(repository = get()) }
    factory { LogoutUseCase(repository = get()) }

    single { HomeApi(client = get<HttpClient>()) }
    single<HomeRepository> { HomeRepositoryImpl(homeApi = get()) }
    factory { GetHomeManifestUseCase(repository = get()) }

    single { ProfileApi(client = get<HttpClient>()) }
    single<ProfileRepository> { ProfileRepositoryImpl(profileApi = get()) }
    factory { GetProfileUseCase(repository = get()) }

    single { BookingApi(client = get<HttpClient>()) }
    single<BookingRepository> { BookingRepositoryImpl(bookingApi = get()) }
    factory { BookClassUseCase(repository = get()) }
    factory { CancelBookingUseCase(repository = get()) }

    single { TimetableApi(client = get<HttpClient>()) }
    single<TimetableRepository> { TimetableRepositoryImpl(api = get()) }
    factory { GetTimetableUseCase(repository = get()) }
}

interface PlatformApiHost {
    val baseUrl: String
}
