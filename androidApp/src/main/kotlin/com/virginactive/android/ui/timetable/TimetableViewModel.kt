package com.virginactive.android.ui.timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.profile.GetProfileUseCase
import com.virginactive.shared.domain.timetable.GetTimetableUseCase
import com.virginactive.shared.domain.timetable.TimetableDay
import com.virginactive.shared.domain.timetable.WeeklyTimetable
import kotlinx.coroutines.launch
import java.util.Calendar

class TimetableViewModel(
    private val getProfile: GetProfileUseCase,
    private val getTimetable: GetTimetableUseCase,
    private val bookClass: BookClassUseCase,
) : ViewModel() {

    var state by mutableStateOf(TimetableUiState())
        private set

    fun load() {
        if (state.isLoading) return
        state = state.copy(isLoading = true, error = null, dateNotice = null)
        viewModelScope.launch {
            val clubId = when (val profile = getProfile()) {
                is AppResult.Ok -> profile.value.homeClub.id
                is AppResult.Err -> ""
            }
            val today = today()
            when (val first = getTimetable(clubId, today)) {
                is AppResult.Ok -> state = state.copy(isLoading = false, week = first.value)
                is AppResult.Err -> handleLoadError(clubId, today, first.error)
            }
        }
    }

    private suspend fun handleLoadError(clubId: String, today: String, error: DomainError) {
        if (error is DomainError.Validation) {
            when (val retry = getTimetable(clubId, today)) {
                is AppResult.Ok -> state = state.copy(
                    isLoading = false,
                    week = retry.value,
                    dateNotice = DATE_NOTICE,
                )

                is AppResult.Err -> state = state.copy(
                    isLoading = false,
                    error = retry.error,
                    dateNotice = DATE_NOTICE,
                )
            }
        } else {
            state = state.copy(isLoading = false, error = error)
        }
    }

    fun selectTab(tab: DayTab) {
        state = state.copy(selectedTab = tab)
    }

    fun book(clubId: String, classId: String) {
        if (state.rowActionInFlight.contains(classId)) return
        state = state.copy(rowActionInFlight = state.rowActionInFlight + classId)
        viewModelScope.launch {
            when (val result = bookClass(clubId, classId)) {
                is AppResult.Ok -> {
                    val newStatus = result.value.toBookingStatus()
                    state = state.copy(
                        week = state.week?.flipRow(classId, newStatus),
                        snackbar = result.value.successMessage(),
                        rowActionInFlight = state.rowActionInFlight - classId,
                    )
                }

                is AppResult.Err -> {
                    state = state.copy(
                        snackbar = "We couldn't complete that. Tap the class to try again.",
                        rowActionInFlight = state.rowActionInFlight - classId,
                    )
                }
            }
        }
    }

    fun snackbarShown() {
        state = state.copy(snackbar = null)
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

    private companion object {
        const val DATE_NOTICE =
            "That date wasn't available, so we're showing this week's classes."
    }
}

private fun BookingOutcome.toBookingStatus(): UserBookingStatus = when (this) {
    is BookingOutcome.Confirmed -> UserBookingStatus.BOOKED
    is BookingOutcome.Waitlisted -> UserBookingStatus.WAITLISTED
    is BookingOutcome.AlreadyBooked ->
        if (wasWaitlist) UserBookingStatus.WAITLISTED else UserBookingStatus.BOOKED
}

private fun BookingOutcome.successMessage(): String = when (this) {
    is BookingOutcome.Confirmed -> "Booked! See you there."
    is BookingOutcome.Waitlisted ->
        position?.let { "You're #$it on the waitlist." } ?: "You're on the waitlist."
    is BookingOutcome.AlreadyBooked ->
        if (wasWaitlist) "You're already on the waitlist." else "You're already booked."
}

private fun WeeklyTimetable.flipRow(classId: String, newStatus: UserBookingStatus): WeeklyTimetable =
    copy(
        days = days.map { day ->
            if (day.classes.none { it.classId == classId }) {
                day
            } else {
                day.copy(
                    classes = day.classes.map { c ->
                        if (c.classId == classId) c.copy(userBookingStatus = newStatus) else c
                    },
                )
            }
        },
    )

data class TimetableUiState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val week: WeeklyTimetable? = null,
    val selectedTab: DayTab = DayTab.All,
    val rowActionInFlight: Set<String> = emptySet(),
    val dateNotice: String? = null,
    val snackbar: String? = null,
)

enum class DayTab(val label: String) {
    All("All"),
    Mon("Mon"),
    Tue("Tue"),
    Wed("Wed"),
    Thu("Thu"),
    Fri("Fri"),
    Sat("Sat"),
    Sun("Sun"),
}

fun WeeklyTimetable.daysFor(tab: DayTab): List<TimetableDay> =
    if (tab == DayTab.All) days else days.filter { it.date.weekdayTab() == tab }

private fun String.weekdayTab(): DayTab? {
    val parts = substringBefore('T').split('-')
    if (parts.size < 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    if (m < 1 || m > 12) return null
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val yy = if (m < 3) y - 1 else y
    val dow = (yy + yy / 4 - yy / 100 + yy / 400 + t[m - 1] + d) % 7
    return when (dow) {
        0 -> DayTab.Sun
        1 -> DayTab.Mon
        2 -> DayTab.Tue
        3 -> DayTab.Wed
        4 -> DayTab.Thu
        5 -> DayTab.Fri
        6 -> DayTab.Sat
        else -> null
    }
}

fun ClassInstance.isActionable(): Boolean =
    userBookingStatus != UserBookingStatus.BOOKED &&
        userBookingStatus != UserBookingStatus.WAITLISTED &&
        status != com.virginactive.shared.domain.booking.ClassStatus.CANCELLED
