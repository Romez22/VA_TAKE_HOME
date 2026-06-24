package com.virginactive.shared.data.booking.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class BookingResponseDto(
    val bookingId: String,
    val status: String,
    val waitlistPosition: Int? = null,
    val classInstance: ClassInstanceDto,
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
