package com.virginactive.shared.data.timetable.dto

import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ClassStatus
import com.virginactive.shared.domain.booking.ClassType
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.timetable.TimetableDay
import com.virginactive.shared.domain.timetable.WeeklyTimetable


internal fun TimetableResponseDto.toDomain(): WeeklyTimetable =
    WeeklyTimetable(
        weekStart = weekStart,
        weekEnd = weekEnd,
        selectedDate = selectedDate,
        days = days.map { it.toDomain() },
    )

internal fun TimetableDayDto.toDomain(): TimetableDay =
    TimetableDay(
        date = date,
        classes = classes.map { it.toDomain() },
    )

internal fun ClassInstanceDto.toDomain(): ClassInstance =
    ClassInstance(
        classId = classId,
        clubId = clubId,
        title = title,
        trainer = trainer,
        type = type.toClassType(),
        startsAt = startsAt,
        endsAt = endsAt,
        spots = spots,
        available = available,
        waitlistCount = waitlistCount,
        status = status.toClassStatus(),
        userBookingStatus = userBookingStatus.toUserBookingStatus(),
    )

private fun String.toClassType(): ClassType =
    ClassType.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: ClassType.UNKNOWN

private fun String.toClassStatus(): ClassStatus =
    ClassStatus.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: ClassStatus.UNKNOWN

private fun String.toUserBookingStatus(): UserBookingStatus =
    UserBookingStatus.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: UserBookingStatus.UNKNOWN
