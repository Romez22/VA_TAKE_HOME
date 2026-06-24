package com.virginactive.shared.di

import com.virginactive.shared.domain.auth.AuthRepository
import com.virginactive.shared.domain.auth.LoginUseCase
import com.virginactive.shared.domain.auth.LogoutUseCase
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.CancelBookingUseCase
import com.virginactive.shared.domain.home.GetHomeManifestUseCase
import com.virginactive.shared.domain.profile.GetProfileUseCase
import com.virginactive.shared.domain.timetable.GetTimetableUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class AppComponent : KoinComponent {
    public val loginUseCase: LoginUseCase by inject()
    public val logoutUseCase: LogoutUseCase by inject()
    public val getHomeManifestUseCase: GetHomeManifestUseCase by inject()
    public val getProfileUseCase: GetProfileUseCase by inject()
    public val bookClassUseCase: BookClassUseCase by inject()
    public val cancelBookingUseCase: CancelBookingUseCase by inject()
    public val getTimetableUseCase: GetTimetableUseCase by inject()
    public val authRepository: AuthRepository by inject()
}
