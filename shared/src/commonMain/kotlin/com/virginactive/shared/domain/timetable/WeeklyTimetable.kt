package com.virginactive.shared.domain.timetable

import com.virginactive.shared.domain.booking.ClassInstance

data class WeeklyTimetable(
    val weekStart: String,
    val weekEnd: String,
    val selectedDate: String,
    val days: List<TimetableDay>,
)

data class TimetableDay(
    val date: String,
    val classes: List<ClassInstance>,
)
