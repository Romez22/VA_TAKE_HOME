package com.virginactive.android.ui.timetable

import com.virginactive.shared.domain.booking.Booking
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.BookingRepository
import com.virginactive.shared.domain.booking.BookingStatus
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ClassStatus
import com.virginactive.shared.domain.booking.ClassType
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import com.virginactive.shared.domain.model.Club
import com.virginactive.shared.domain.model.MembershipTier
import com.virginactive.shared.domain.model.UserProfile
import com.virginactive.shared.domain.profile.GetProfileUseCase
import com.virginactive.shared.domain.profile.ProfileRepository
import com.virginactive.shared.domain.timetable.GetTimetableUseCase
import com.virginactive.shared.domain.timetable.TimetableDay
import com.virginactive.shared.domain.timetable.TimetableRepository
import com.virginactive.shared.domain.timetable.WeeklyTimetable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }


    private class FakeProfileRepository(
        var result: AppResult<UserProfile>,
    ) : ProfileRepository {
        override suspend fun getProfile(): AppResult<UserProfile> = result
    }

    private class FakeTimetableRepository(
        val results: ArrayDeque<AppResult<WeeklyTimetable>>,
    ) : TimetableRepository {
        var calls = 0
        val datesRequested = mutableListOf<String>()
        override suspend fun getWeek(clubId: String, date: String): AppResult<WeeklyTimetable> {
            calls++
            datesRequested += date
            return results.removeFirst()
        }
    }

    private class FakeBookingRepository(
        var result: AppResult<BookingOutcome>,
    ) : BookingRepository {
        var bookCalls = 0
        val classIdsBooked = mutableListOf<String>()
        override suspend fun book(clubId: String, classId: String): AppResult<BookingOutcome> {
            bookCalls++
            classIdsBooked += classId
            return result
        }

        override suspend fun cancel(clubId: String, classId: String): AppResult<Unit> =
            AppResult.Ok(Unit)
    }

    private val profile = UserProfile(
        id = "u1",
        firstName = "Avid",
        lastName = "Runner",
        email = "avid@example.com",
        membershipTier = MembershipTier.PREMIUM,
        homeClub = Club(id = "club_sea_point", name = "Sea Point"),
    )

    private fun classInstance(
        classId: String,
        status: ClassStatus = ClassStatus.AVAILABLE,
        userBookingStatus: UserBookingStatus = UserBookingStatus.NONE,
    ) = ClassInstance(
        classId = classId,
        clubId = "club_sea_point",
        title = "Morning Spin",
        trainer = "Sam",
        type = ClassType.SPIN,
        startsAt = "2026-06-24T12:00:00+02:00",
        endsAt = "2026-06-24T12:45:00+02:00",
        spots = 20,
        available = 8,
        waitlistCount = 0,
        status = status,
        userBookingStatus = userBookingStatus,
    )

    private fun week(vararg classes: ClassInstance) = WeeklyTimetable(
        weekStart = "2026-06-22",
        weekEnd = "2026-06-28",
        selectedDate = "2026-06-24",
        days = listOf(TimetableDay(date = "2026-06-24", classes = classes.toList())),
    )

    private fun vm(
        timetable: FakeTimetableRepository,
        booking: FakeBookingRepository = FakeBookingRepository(
            AppResult.Ok(BookingOutcome.AlreadyBooked(wasWaitlist = false)),
        ),
        profileRepo: FakeProfileRepository = FakeProfileRepository(AppResult.Ok(profile)),
    ) = TimetableViewModel(
        getProfile = GetProfileUseCase(profileRepo),
        getTimetable = GetTimetableUseCase(timetable),
        bookClass = BookClassUseCase(booking),
    )


    @Test
    fun `load fetches the week once and stores it`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))))
        val sut = vm(timetable)

        sut.load()
        advanceUntilIdle()

        assertEquals(1, timetable.calls)
        assertNotNull(sut.state.week)
        assertNull(sut.state.error)
        assertTrue(!sut.state.isLoading)
    }

    @Test
    fun `selectTab performs no network call`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))))
        val sut = vm(timetable)
        sut.load()
        advanceUntilIdle()

        sut.selectTab(DayTab.Mon)
        advanceUntilIdle()

        assertEquals(1, timetable.calls)
        assertEquals(DayTab.Mon, sut.state.selectedTab)
    }

    @Test
    fun `book passes classId unchanged and flips the row to BOOKED on Confirmed`() =
        runTest(testDispatcher) {
            val booking = FakeBookingRepository(
                AppResult.Ok(
                    BookingOutcome.Confirmed(
                        Booking(
                            bookingId = "b1",
                            status = BookingStatus.BOOKED,
                            waitlistPosition = null,
                            classInstance = classInstance("c1"),
                        ),
                    ),
                ),
            )
            val timetable = FakeTimetableRepository(ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))))
            val sut = vm(timetable, booking)
            sut.load()
            advanceUntilIdle()

            sut.book(clubId = "club_sea_point", classId = "c1")
            advanceUntilIdle()

            assertEquals(1, booking.bookCalls)
            assertEquals("c1", booking.classIdsBooked.single())
            val row = sut.state.week!!.days.flatMap { it.classes }.single { it.classId == "c1" }
            assertEquals(UserBookingStatus.BOOKED, row.userBookingStatus)
            assertNotNull(sut.state.snackbar)
            assertTrue(!sut.state.rowActionInFlight.contains("c1"))
        }

    @Test
    fun `book flips the row to WAITLISTED on Waitlisted`() = runTest(testDispatcher) {
        val booking = FakeBookingRepository(
            AppResult.Ok(
                BookingOutcome.Waitlisted(
                    Booking(
                        bookingId = "b1",
                        status = BookingStatus.WAITLISTED,
                        waitlistPosition = 3,
                        classInstance = classInstance("c1", status = ClassStatus.FULL),
                    ),
                    position = 3,
                ),
            ),
        )
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1", status = ClassStatus.FULL))))),
        )
        val sut = vm(timetable, booking)
        sut.load()
        advanceUntilIdle()

        sut.book(clubId = "club_sea_point", classId = "c1")
        advanceUntilIdle()

        val row = sut.state.week!!.days.flatMap { it.classes }.single { it.classId == "c1" }
        assertEquals(UserBookingStatus.WAITLISTED, row.userBookingStatus)
    }

    @Test
    fun `book failure surfaces a retry snackbar and does not flip the row`() =
        runTest(testDispatcher) {
            val booking = FakeBookingRepository(AppResult.Err(DomainError.Server))
            val timetable = FakeTimetableRepository(ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))))
            val sut = vm(timetable, booking)
            sut.load()
            advanceUntilIdle()

            sut.book(clubId = "club_sea_point", classId = "c1")
            advanceUntilIdle()

            assertEquals(1, booking.bookCalls)
            val row = sut.state.week!!.days.flatMap { it.classes }.single { it.classId == "c1" }
            assertEquals(UserBookingStatus.NONE, row.userBookingStatus)
            assertNotNull(sut.state.snackbar)
            assertTrue(!sut.state.rowActionInFlight.contains("c1"))
        }

    @Test
    fun `validation date error sets a dateNotice and falls back to the current week`() =
        runTest(testDispatcher) {
            val timetable = FakeTimetableRepository(
                ArrayDeque(
                    listOf(
                        AppResult.Err(DomainError.Validation("InvalidDate")),
                        AppResult.Ok(week(classInstance("c1"))),
                    ),
                ),
            )
            val sut = vm(timetable)

            sut.load()
            advanceUntilIdle()

            assertNotNull(sut.state.dateNotice)
            assertNotNull(sut.state.week)
            assertNull(sut.state.error)
        }
}
