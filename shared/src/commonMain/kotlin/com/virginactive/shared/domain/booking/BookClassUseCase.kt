package com.virginactive.shared.domain.booking

import com.virginactive.shared.domain.error.AppResult

class BookClassUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(clubId: String, classId: String): AppResult<BookingOutcome> =
        repository.book(clubId, classId)
}
