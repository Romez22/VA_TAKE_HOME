package com.virginactive.shared.domain.timetable

import com.virginactive.shared.domain.error.AppResult

class GetTimetableUseCase(private val repository: TimetableRepository) {
    suspend operator fun invoke(clubId: String, date: String): AppResult<WeeklyTimetable> =
        repository.getWeek(clubId, date)
}
