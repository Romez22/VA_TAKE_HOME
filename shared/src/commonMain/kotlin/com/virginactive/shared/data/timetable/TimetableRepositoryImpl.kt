package com.virginactive.shared.data.timetable

import com.virginactive.shared.data.remote.appJson
import com.virginactive.shared.data.remote.safeCall
import com.virginactive.shared.data.timetable.dto.TimetableResponseDto
import com.virginactive.shared.data.timetable.dto.toDomain
import com.virginactive.shared.domain.error.AppResult
import com.virginactive.shared.domain.timetable.TimetableRepository
import com.virginactive.shared.domain.timetable.WeeklyTimetable

internal class TimetableRepositoryImpl(
    private val api: TimetableApi,
) : TimetableRepository {

    override suspend fun getWeek(clubId: String, date: String): AppResult<WeeklyTimetable> =
        safeCall { appJson.decodeFromString<TimetableResponseDto>(api.getWeek(clubId, date)).toDomain() }
}
