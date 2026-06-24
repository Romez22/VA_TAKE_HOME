package com.virginactive.shared.data.timetable.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TimetableResponseDto(
    val clubId: String,
    val weekStart: String,
    val weekEnd: String,
    val selectedDate: String,
    val days: List<TimetableDayDto>,
)

@Serializable
internal data class TimetableDayDto(
    val date: String,
    val classes: List<ClassInstanceDto>,
)

@Serializable
internal data class ClassInstanceDto(
    val classId: String,
    val clubId: String,
    val title: String,
    val trainer: String,
    val type: String,
    val startsAt: String,
    val endsAt: String,
    val spots: Int,
    val available: Int,
    val waitlistCount: Int,
    val status: String,
    val userBookingStatus: String,
)
