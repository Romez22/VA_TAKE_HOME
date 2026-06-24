package com.virginactive.android.ui.detail

import com.virginactive.shared.domain.booking.Booking
import com.virginactive.shared.domain.booking.BookClassUseCase
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.BookingRepository
import com.virginactive.shared.domain.booking.BookingStatus
import com.virginactive.shared.domain.booking.CancelBookingUseCase
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
class ClassDetailViewModelTest {

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
        override suspend fun getWeek(clubId: String, date: String): AppResult<WeeklyTimetable> {
            calls++
            return results.removeFirst()
        }
    }

    private class FakeBookingRepository(
        var bookResult: AppResult<BookingOutcome>,
        var cancelResult: AppResult<Unit> = AppResult.Ok(Unit),
    ) : BookingRepository {
        var bookCalls = 0
        var cancelCalls = 0
        val classIdsBooked = mutableListOf<String>()
        val classIdsCancelled = mutableListOf<String>()
        override suspend fun book(clubId: String, classId: String): AppResult<BookingOutcome> {
            bookCalls++
            classIdsBooked += classId
            return bookResult
        }

        override suspend fun cancel(clubId: String, classId: String): AppResult<Unit> {
            cancelCalls++
            classIdsCancelled += classId
            return cancelResult
        }
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
        startsAt = "2099-06-24T12:00:00+02:00",
        endsAt = "2099-06-24T12:45:00+02:00",
        spots = 20,
        available = 8,
        waitlistCount = 0,
        status = status,
        userBookingStatus = userBookingStatus,
    )

    private fun week(vararg classes: ClassInstance) = WeeklyTimetable(
        weekStart = "2099-06-22",
        weekEnd = "2099-06-28",
        selectedDate = "2099-06-24",
        days = listOf(TimetableDay(date = "2099-06-24", classes = classes.toList())),
    )

    private fun vm(
        timetable: FakeTimetableRepository,
        booking: FakeBookingRepository = FakeBookingRepository(
            AppResult.Ok(BookingOutcome.AlreadyBooked(wasWaitlist = false)),
        ),
        profileRepo: FakeProfileRepository = FakeProfileRepository(AppResult.Ok(profile)),
    ) = ClassDetailViewModel(
        getProfile = GetProfileUseCase(profileRepo),
        getTimetable = GetTimetableUseCase(timetable),
        bookClass = BookClassUseCase(booking),
        cancelBooking = CancelBookingUseCase(booking),
    )


    @Test
    fun `load resolves the routed class from the week once`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"), classInstance("c2"))))),
        )
        val sut = vm(timetable)

        sut.load("c2")
        advanceUntilIdle()

        assertEquals(1, timetable.calls)
        assertEquals("c2", sut.state.classInstance?.classId)
        assertEquals("club_sea_point", sut.state.clubId)
        assertNull(sut.state.error)
        assertTrue(!sut.state.isLoading)
    }

    @Test
    fun `load seeds the booked banner from the loaded userBookingStatus`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(
            ArrayDeque(
                listOf(
                    AppResult.Ok(
                        week(classInstance("c1", userBookingStatus = UserBookingStatus.BOOKED)),
                    ),
                ),
            ),
        )
        val sut = vm(timetable)

        sut.load("c1")
        advanceUntilIdle()

        assertEquals(UserBookingStatus.BOOKED, sut.state.confirmation?.userBookingStatus)
    }

    @Test
    fun `load sets an error when the routed classId is not in the week`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))),
        )
        val sut = vm(timetable)

        sut.load("missing")
        advanceUntilIdle()

        assertNotNull(sut.state.error)
        assertNull(sut.state.classInstance)
    }

    @Test
    fun `load surfaces a load error`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Err(DomainError.Server))),
        )
        val sut = vm(timetable)

        sut.load("c1")
        advanceUntilIdle()

        assertEquals(DomainError.Server, sut.state.error)
    }

    @Test
    fun `book passes the routed classId unchanged and confirms with the bookingId`() =
        runTest(testDispatcher) {
            val booking = FakeBookingRepository(
                AppResult.Ok(
                    BookingOutcome.Confirmed(
                        Booking(
                            bookingId = "bk_123",
                            status = BookingStatus.BOOKED,
                            waitlistPosition = null,
                            classInstance = classInstance("c1"),
                        ),
                    ),
                ),
            )
            val timetable = FakeTimetableRepository(
                ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))),
            )
            val sut = vm(timetable, booking)
            sut.load("c1")
            advanceUntilIdle()

            sut.book()
            advanceUntilIdle()

            assertEquals(1, booking.bookCalls)
            assertEquals("c1", booking.classIdsBooked.single())
            assertEquals(UserBookingStatus.BOOKED, sut.state.confirmation?.userBookingStatus)
            assertEquals("bk_123", sut.state.confirmation?.bookingId)
            assertNull(sut.state.actionError)
            assertTrue(!sut.state.actionInFlight)
        }

    @Test
    fun `book maps Waitlisted to WAITLISTED with the position`() = runTest(testDispatcher) {
        val booking = FakeBookingRepository(
            AppResult.Ok(
                BookingOutcome.Waitlisted(
                    Booking(
                        bookingId = "bk_w",
                        status = BookingStatus.WAITLISTED,
                        waitlistPosition = 4,
                        classInstance = classInstance("c1", status = ClassStatus.FULL),
                    ),
                    position = 4,
                ),
            ),
        )
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1", status = ClassStatus.FULL))))),
        )
        val sut = vm(timetable, booking)
        sut.load("c1")
        advanceUntilIdle()

        sut.book()
        advanceUntilIdle()

        assertEquals(UserBookingStatus.WAITLISTED, sut.state.confirmation?.userBookingStatus)
        assertEquals(4, sut.state.confirmation?.waitlistPosition)
    }

    @Test
    fun `book failure sets actionError and leaves the confirmation unchanged`() =
        runTest(testDispatcher) {
            val booking = FakeBookingRepository(AppResult.Err(DomainError.Server))
            val timetable = FakeTimetableRepository(
                ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))),
            )
            val sut = vm(timetable, booking)
            sut.load("c1")
            advanceUntilIdle()

            sut.book()
            advanceUntilIdle()

            assertEquals(1, booking.bookCalls)
            assertEquals(DomainError.Server, sut.state.actionError)
            assertEquals(UserBookingStatus.NONE, sut.state.confirmation?.userBookingStatus)
            assertTrue(!sut.state.actionInFlight)
        }

    @Test
    fun `cancel clears the confirmation on Ok`() = runTest(testDispatcher) {
        val booking = FakeBookingRepository(
            bookResult = AppResult.Ok(BookingOutcome.AlreadyBooked(wasWaitlist = false)),
            cancelResult = AppResult.Ok(Unit),
        )
        val timetable = FakeTimetableRepository(
            ArrayDeque(
                listOf(
                    AppResult.Ok(
                        week(classInstance("c1", userBookingStatus = UserBookingStatus.BOOKED)),
                    ),
                ),
            ),
        )
        val sut = vm(timetable, booking)
        sut.load("c1")
        advanceUntilIdle()

        sut.cancel()
        advanceUntilIdle()

        assertEquals(1, booking.cancelCalls)
        assertEquals("c1", booking.classIdsCancelled.single())
        assertEquals(UserBookingStatus.NONE, sut.state.confirmation?.userBookingStatus)
    }

    @Test
    fun `cancel failure sets actionError`() = runTest(testDispatcher) {
        val booking = FakeBookingRepository(
            bookResult = AppResult.Ok(BookingOutcome.AlreadyBooked(wasWaitlist = false)),
            cancelResult = AppResult.Err(DomainError.NotFound("BookingNotFound")),
        )
        val timetable = FakeTimetableRepository(
            ArrayDeque(
                listOf(
                    AppResult.Ok(
                        week(classInstance("c1", userBookingStatus = UserBookingStatus.BOOKED)),
                    ),
                ),
            ),
        )
        val sut = vm(timetable, booking)
        sut.load("c1")
        advanceUntilIdle()

        sut.cancel()
        advanceUntilIdle()

        assertEquals(DomainError.NotFound("BookingNotFound"), sut.state.actionError)
    }

    @Test
    fun `isWithinForfeitWindow is false for a class far in the future`() = runTest(testDispatcher) {
        val timetable = FakeTimetableRepository(
            ArrayDeque(listOf(AppResult.Ok(week(classInstance("c1"))))),
        )
        val sut = vm(timetable)
        sut.load("c1")
        advanceUntilIdle()

        assertTrue(!sut.isWithinForfeitWindow())
    }
}
