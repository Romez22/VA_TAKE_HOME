package com.virginactive.android.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.CancelBookingUseCase
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ForfeitPolicy
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.profile.GetProfileUseCase
import com.virginactive.shared.domain.timetable.GetTimetableUseCase
import kotlinx.coroutines.launch
import java.util.Calendar

class ClassDetailViewModel(
    private val getProfile: GetProfileUseCase,
    private val getTimetable: GetTimetableUseCase,
    private val bookClass: BookClassUseCase,
    private val cancelBooking: CancelBookingUseCase,
) : ViewModel() {

    var state by mutableStateOf(ClassDetailUiState())
        private set

    fun load(classId: String) {
        if (state.isLoading) return
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val clubId = when (val profile = getProfile()) {
                is AppResult.Ok -> profile.value.homeClub.id
                is AppResult.Err -> ""
            }
            when (val week = getTimetable(clubId, today())) {
                is AppResult.Ok -> {
                    val instance = week.value.days
                        .flatMap { it.classes }
                        .firstOrNull { it.classId == classId }
                    if (instance == null) {
                        state = state.copy(
                            isLoading = false,
                            error = DomainError.NotFound("ClassNotFound"),
                            clubId = clubId,
                        )
                    } else {
                        state = state.copy(
                            isLoading = false,
                            classInstance = instance,
                            clubId = clubId,
                            confirmation = Confirmation(
                                bookingId = null,
                                userBookingStatus = instance.userBookingStatus,
                                waitlistPosition = null,
                            ),
                        )
                    }
                }

                is AppResult.Err -> state = state.copy(isLoading = false, error = week.error)
            }
        }
    }

    fun book() {
        val instance = state.classInstance ?: return
        val clubId = state.clubId ?: return
        if (state.actionInFlight) return
        state = state.copy(actionInFlight = true, actionError = null)
        viewModelScope.launch {
            when (val result = bookClass(clubId, instance.classId)) {
                is AppResult.Ok -> state = state.copy(
                    actionInFlight = false,
                    confirmation = result.value.toConfirmation(),
                )

                is AppResult.Err -> state = state.copy(
                    actionInFlight = false,
                    actionError = result.error,
                )
            }
        }
    }

    fun cancel() {
        val instance = state.classInstance ?: return
        val clubId = state.clubId ?: return
        if (state.actionInFlight) return
        state = state.copy(actionInFlight = true, actionError = null)
        viewModelScope.launch {
            when (val result = cancelBooking(clubId, instance.classId)) {
                is AppResult.Ok -> state = state.copy(
                    actionInFlight = false,
                    confirmation = Confirmation(
                        bookingId = null,
                        userBookingStatus = UserBookingStatus.NONE,
                        waitlistPosition = null,
                    ),
                )

                is AppResult.Err -> state = state.copy(
                    actionInFlight = false,
                    actionError = result.error,
                )
            }
        }
    }

    fun actionErrorShown() {
        state = state.copy(actionError = null)
    }

    fun isWithinForfeitWindow(): Boolean {
        val startsAt = state.classInstance?.startsAt ?: return false
        return ForfeitPolicy.isWithinForfeitWindow(startsAt)
    }

    private fun today(): String {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH) + 1
        val day = now.get(Calendar.DAY_OF_MONTH)
        return buildString {
            append(year.toString().padStart(4, '0'))
            append('-')
            append(month.toString().padStart(2, '0'))
            append('-')
            append(day.toString().padStart(2, '0'))
        }
    }
}

private fun BookingOutcome.toConfirmation(): Confirmation = when (this) {
    is BookingOutcome.Confirmed -> Confirmation(
        bookingId = booking.bookingId,
        userBookingStatus = UserBookingStatus.BOOKED,
        waitlistPosition = null,
    )

    is BookingOutcome.Waitlisted -> Confirmation(
        bookingId = booking.bookingId,
        userBookingStatus = UserBookingStatus.WAITLISTED,
        waitlistPosition = position,
    )

    is BookingOutcome.AlreadyBooked -> Confirmation(
        bookingId = null,
        userBookingStatus = if (wasWaitlist) UserBookingStatus.WAITLISTED else UserBookingStatus.BOOKED,
        waitlistPosition = null,
    )
}

data class Confirmation(
    val bookingId: String?,
    val userBookingStatus: UserBookingStatus,
    val waitlistPosition: Int?,
)

data class ClassDetailUiState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val classInstance: ClassInstance? = null,
    val clubId: String? = null,
    val confirmation: Confirmation? = null,
    val actionInFlight: Boolean = false,
    val actionError: DomainError? = null,
)
