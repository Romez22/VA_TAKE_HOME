package com.virginactive.shared.domain.timetable

import com.virginactive.shared.domain.error.AppResult

interface TimetableRepository {
    suspend fun getWeek(clubId: String, date: String): AppResult<WeeklyTimetable>
}
