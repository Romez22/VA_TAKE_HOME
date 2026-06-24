package com.virginactive.shared.domain.booking

sealed interface BookingOutcome {
    data class Confirmed(val booking: Booking) : BookingOutcome

    data class Waitlisted(val booking: Booking, val position: Int?) : BookingOutcome

    data class AlreadyBooked(val wasWaitlist: Boolean) : BookingOutcome
}
