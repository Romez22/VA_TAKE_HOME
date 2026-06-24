package com.virginactive.shared.domain.booking

enum class ClassType {
    GROUP_WORKOUT,
    YOGA,
    SPIN,
    PILATES,
    HIIT,
    SWIMMING,
    UNKNOWN,
}

enum class ClassStatus {
    AVAILABLE,
    FULL,
    CANCELLED,
    UNKNOWN,
}

enum class UserBookingStatus {
    NONE,
    BOOKED,
    WAITLISTED,
    UNKNOWN,
}

enum class BookingStatus {
    BOOKED,
    WAITLISTED,
    UNKNOWN,
}

data class ClassInstance(
    val classId: String,
    val clubId: String,
    val title: String,
    val trainer: String,
    val type: ClassType,
    val startsAt: String,
    val endsAt: String,
    val spots: Int,
    val available: Int,
    val waitlistCount: Int,
    val status: ClassStatus,
    val userBookingStatus: UserBookingStatus,
)

data class Booking(
    val bookingId: String,
    val status: BookingStatus,
    val waitlistPosition: Int?,
    val classInstance: ClassInstance,
)
