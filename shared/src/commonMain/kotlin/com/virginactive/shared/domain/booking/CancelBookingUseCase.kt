package com.virginactive.shared.domain.booking

import com.virginactive.shared.domain.error.AppResult

class CancelBookingUseCase(private val repository: BookingRepository) {
    suspend operator fun invoke(clubId: String, classId: String): AppResult<Unit> =
        repository.cancel(clubId, classId)
}
