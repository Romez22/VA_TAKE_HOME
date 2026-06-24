package com.virginactive.shared.data.booking.dto

import com.virginactive.shared.domain.booking.Booking
import com.virginactive.shared.domain.booking.BookingStatus
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ClassStatus
import com.virginactive.shared.domain.booking.ClassType
import com.virginactive.shared.domain.booking.UserBookingStatus


internal fun BookingResponseDto.toDomain(): Booking =
    Booking(
        bookingId = bookingId,
        status = status.toBookingStatus(),
        waitlistPosition = waitlistPosition,
        classInstance = classInstance.toDomain(),
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

private fun String.toBookingStatus(): BookingStatus =
    BookingStatus.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: BookingStatus.UNKNOWN

private fun String.toClassType(): ClassType =
    ClassType.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: ClassType.UNKNOWN

private fun String.toClassStatus(): ClassStatus =
    ClassStatus.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: ClassStatus.UNKNOWN

private fun String.toUserBookingStatus(): UserBookingStatus =
    UserBookingStatus.entries.firstOrNull { it.name.replace("_", "").equals(this, ignoreCase = true) }
        ?: UserBookingStatus.UNKNOWN
