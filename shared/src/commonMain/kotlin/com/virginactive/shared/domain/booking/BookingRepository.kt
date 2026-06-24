package com.virginactive.shared.domain.booking

import com.virginactive.shared.domain.error.AppResult

interface BookingRepository {

    suspend fun book(clubId: String, classId: String): AppResult<BookingOutcome>

    suspend fun cancel(clubId: String, classId: String): AppResult<Unit>
}
