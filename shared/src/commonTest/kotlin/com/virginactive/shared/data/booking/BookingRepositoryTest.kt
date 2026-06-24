package com.virginactive.shared.data.booking

import com.virginactive.shared.data.auth.apiErrorJson
import com.virginactive.shared.data.remote.ApiConfig
import com.virginactive.shared.data.remote.createHttpClient
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.BookingRepository
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.error.DomainError
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BookingRepositoryTest {

    private val config = ApiConfig(baseUrl = "http://test.local")

    private companion object {
        const val CLUB_ID = "club_sea_point"

        const val COMPOSITE_CLASS_ID = "sp-lunchtime-spin::2026-06-24"

        const val EXPECTED_BOOKING_PATH =
            "/clubs/club_sea_point/classes/sp-lunchtime-spin%3A%3A2026-06-24/bookings"
    }

    private fun bookingRepository(engine: MockEngine): BookingRepository {
        val client = createHttpClient(engine = engine, config = config, enableLogging = false)
        val api = BookingApi(client)
        return BookingRepositoryImpl(api)
    }


    @Test
    fun book201MapsToConfirmedCarryingBookingIdAndClassDetails() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Created,
            body = bookingResponseJson(
                bookingId = "5cb53d29-aaaa",
                status = "booked",
                waitlistPosition = null,
                classId = COMPOSITE_CLASS_ID,
                title = "Lunchtime Spin",
            ),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertIs<AppResult.Ok<*>>(result, "201 -> Ok")
        val outcome = (result as AppResult.Ok).value
        assertIs<BookingOutcome.Confirmed>(outcome, "201 -> Confirmed (BOOK-02)")
        assertEquals("5cb53d29-aaaa", outcome.booking.bookingId, "Confirmed carries bookingId (BOOK-03)")
    }

    @Test
    fun book202MapsToWaitlistedWithPosition() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Accepted,
            body = bookingResponseJson(
                bookingId = "42bd-bbbb",
                status = "waitlisted",
                waitlistPosition = 1,
                classStatus = "full",
                userBookingStatus = "waitlisted",
            ),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertIs<AppResult.Ok<*>>(result, "202 -> Ok")
        val outcome = (result as AppResult.Ok).value
        assertIs<BookingOutcome.Waitlisted>(outcome, "202 -> Waitlisted (BOOK-02)")
        assertEquals(1, outcome.position, "Waitlisted carries waitlistPosition == 1 (BOOK-02)")
    }

    @Test
    fun book409AlreadyBookedMapsToSuccessEquivalentNotError() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Conflict,
            body = apiErrorJson(error = "AlreadyBooked", code = null, message = "You are already booked."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertIs<AppResult.Ok<*>>(result, "409 AlreadyBooked -> Ok (success-equivalent, BOOK-04)")
        val outcome = (result as AppResult.Ok).value
        assertIs<BookingOutcome.AlreadyBooked>(outcome, "409 AlreadyBooked -> AlreadyBooked")
        assertEquals(false, outcome.wasWaitlist, "AlreadyBooked: wasWaitlist == false")
    }

    @Test
    fun book409AlreadyWaitlistedMapsToSuccessEquivalentWasWaitlistTrue() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Conflict,
            body = apiErrorJson(error = "AlreadyWaitlisted", code = null, message = "Already on the waitlist."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertIs<AppResult.Ok<*>>(result, "409 AlreadyWaitlisted -> Ok (success-equivalent, BOOK-04)")
        val outcome = (result as AppResult.Ok).value
        assertIs<BookingOutcome.AlreadyBooked>(outcome, "409 AlreadyWaitlisted -> AlreadyBooked")
        assertEquals(true, outcome.wasWaitlist, "AlreadyWaitlisted: wasWaitlist == true (BOOK-04)")
    }

    @Test
    fun book409UnknownSubCodeMapsToErrConflictNotSuccessEquivalent() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Conflict,
            body = apiErrorJson(error = "MembershipSuspended", code = null, message = "Membership suspended."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(
            AppResult.Err(DomainError.Conflict("MembershipSuspended")),
            result,
            "unknown 409 sub-code -> Err(Conflict), NOT success-equivalent AlreadyBooked (CR-01, BOOK-04)",
        )
    }

    @Test
    fun book422ClassInPastMapsToErrClassInPast() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.UnprocessableEntity,
            body = apiErrorJson(error = "ClassInPast", code = null, message = "This class has already started."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(AppResult.Err(DomainError.ClassInPast), result, "422 ClassInPast -> Err(ClassInPast) (BOOK-04)")
    }

    @Test
    fun book404ClassNotFoundReadsErrorFieldNotCode() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.NotFound,
            body = apiErrorJson(error = "ClassNotFound", code = null, message = "The requested class was not found."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(
            AppResult.Err(DomainError.NotFound("ClassNotFound")),
            result,
            "404 -> Err(NotFound) resolved from the `error` field, NOT `code` (BOOK-04)",
        )
    }


    @Test
    fun bookSingleEncodesCompositeClassIdInRecordedPath() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.Created,
            body = bookingResponseJson(classId = COMPOSITE_CLASS_ID),
        )
        val repo = bookingRepository(engine)

        repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(
            EXPECTED_BOOKING_PATH,
            recorder.lastEncodedPath,
            "composite classId single-encoded `::` -> `%3A%3A` in the recorded path (BOOK-05)",
        )
        assertEquals(HttpMethod.Post, recorder.methods.last(), "book issues a POST")
    }


    @Test
    fun book500SurfacesErrServerAndIsAttemptedExactlyOnce() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.InternalServerError,
            body = apiErrorJson(error = "ChaosFailure", code = null, message = "The service is temporarily unavailable."),
        )
        val repo = bookingRepository(engine)

        val result = repo.book(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(AppResult.Err(DomainError.Server), result, "500 on POST -> Err(Server) (BOOK-08)")
        assertEquals(1, recorder.attempts, "booking POST attempted exactly once — never auto-retried (BOOK-08)")
    }


    @Test
    fun cancel204MapsToOkUnit() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(recorder = recorder, status = HttpStatusCode.NoContent)
        val repo = bookingRepository(engine)

        val result = repo.cancel(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(AppResult.Ok(Unit), result, "204 -> Ok(Unit) (BOOK-06)")
        assertEquals(HttpMethod.Delete, recorder.methods.last(), "cancel issues a DELETE")
    }

    @Test
    fun cancel404BookingNotFoundReadsErrorFieldDistinctFromClassNotFound() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(
            recorder = recorder,
            status = HttpStatusCode.NotFound,
            body = apiErrorJson(error = "BookingNotFound", code = null, message = "No booking exists for this class."),
        )
        val repo = bookingRepository(engine)

        val result = repo.cancel(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(
            AppResult.Err(DomainError.NotFound("BookingNotFound")),
            result,
            "404 on cancel -> Err(NotFound(\"BookingNotFound\")), distinct from ClassNotFound (BOOK-06)",
        )
    }

    @Test
    fun cancelSingleEncodesCompositeClassIdInRecordedPath() = runTest {
        val recorder = BookingCallRecorder()
        val engine = recordingBookingEngine(recorder = recorder, status = HttpStatusCode.NoContent)
        val repo = bookingRepository(engine)

        repo.cancel(CLUB_ID, COMPOSITE_CLASS_ID)

        assertEquals(
            EXPECTED_BOOKING_PATH,
            recorder.lastEncodedPath,
            "cancel single-encodes the composite classId too (BOOK-05/BOOK-06)",
        )
        assertTrue(recorder.attempts >= 1, "cancel reaches the engine")
    }
}
