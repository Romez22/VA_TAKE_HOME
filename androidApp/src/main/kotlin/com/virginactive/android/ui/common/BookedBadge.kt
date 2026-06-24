package com.virginactive.android.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.virginactive.android.theme.OnSuccessContainer
import com.virginactive.android.theme.Spacing
import com.virginactive.android.theme.SuccessContainer
import com.virginactive.shared.domain.booking.UserBookingStatus

@Composable
fun BookedBadge(
    status: UserBookingStatus,
    modifier: Modifier = Modifier,
) {
    val label = when (status) {
        UserBookingStatus.BOOKED -> "Booked"
        UserBookingStatus.WAITLISTED -> "Waitlisted"
        UserBookingStatus.NONE, UserBookingStatus.UNKNOWN -> return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = OnSuccessContainer,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(SuccessContainer)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}
