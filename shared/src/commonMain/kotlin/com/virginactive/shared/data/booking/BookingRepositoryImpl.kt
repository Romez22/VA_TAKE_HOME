package com.virginactive.shared.data.booking

import com.virginactive.shared.data.booking.dto.toDomain
import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.domain.booking.BookingOutcome
import com.virginactive.shared.domain.booking.BookingRepository
import com.virginactive.shared.domain.error.AppResult

internal class BookingRepositoryImpl(
    private val bookingApi: BookingApi,
) : BookingRepository {

    override suspend fun book(clubId: String, classId: String): AppResult<BookingOutcome> =
        safeCall {
            when (val result = bookingApi.book(clubId, classId)) {
                is BookingResult.Confirmed -> BookingOutcome.Confirmed(result.dto.toDomain())
                is BookingResult.Waitlisted ->
                    BookingOutcome.Waitlisted(result.dto.toDomain(), result.dto.waitlistPosition)
                is BookingResult.Already -> BookingOutcome.AlreadyBooked(result.wasWaitlist)
            }
        }

    override suspend fun cancel(clubId: String, classId: String): AppResult<Unit> =
        safeCall { bookingApi.cancel(clubId, classId) }
}
