package com.virginactive.android.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.virginactive.android.theme.CardCornerRadius
import com.virginactive.android.theme.OnSuccessContainer
import com.virginactive.android.theme.Spacing
import com.virginactive.android.theme.SuccessContainer
import com.virginactive.shared.domain.booking.UserBookingStatus

@Composable
fun ConfirmationContent(
    confirmation: Confirmation,
    modifier: Modifier = Modifier,
) {
    when (confirmation.userBookingStatus) {
        UserBookingStatus.BOOKED -> {
            val bookingId = confirmation.bookingId
            if (bookingId != null) {
                Banner(modifier = modifier, heading = "Booking Confirmed!", body = "Booking ID: $bookingId")
            } else {
                Banner(modifier = modifier, heading = "You are booked for this class", body = null)
            }
        }

        UserBookingStatus.WAITLISTED -> {
            val position = confirmation.waitlistPosition
            Banner(
                modifier = modifier,
                heading = "You're on the waitlist",
                body = position?.let { "Position $it" },
            )
        }

        UserBookingStatus.NONE, UserBookingStatus.UNKNOWN -> Unit
    }
}

@Composable
private fun Banner(
    heading: String,
    body: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(SuccessContainer)
            .padding(Spacing.md),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleLarge,
            color = OnSuccessContainer,
        )
        if (body != null) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSuccessContainer,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}
