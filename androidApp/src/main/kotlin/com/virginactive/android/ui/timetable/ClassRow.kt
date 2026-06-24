package com.virginactive.android.ui.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.virginactive.android.theme.Spacing
import com.virginactive.android.ui.common.BookedBadge
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ClassStatus
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.home.DateTimeDisplay

@Composable
fun ClassRow(
    instance: ClassInstance,
    inFlight: Boolean,
    onBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCancelled = instance.status == ClassStatus.CANCELLED
    val rowAlpha = if (isCancelled) DISABLED_ALPHA else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Column(modifier = Modifier.width(72.dp)) {
            Text(
                text = DateTimeDisplay.timeLabel(instance.startsAt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = DateTimeDisplay.timeLabel(instance.endsAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instance.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = instance.trainer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ClassRowTrailing(
            instance = instance,
            isCancelled = isCancelled,
            inFlight = inFlight,
            onBook = onBook,
        )
    }
}

@Composable
private fun ClassRowTrailing(
    instance: ClassInstance,
    isCancelled: Boolean,
    inFlight: Boolean,
    onBook: () -> Unit,
) {
    when {
        instance.userBookingStatus == UserBookingStatus.BOOKED ||
            instance.userBookingStatus == UserBookingStatus.WAITLISTED ->
            BookedBadge(status = instance.userBookingStatus)

        isCancelled || !instance.isActionable() ->
            Text(
                text = instance.status.label(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

        else -> Column(horizontalAlignment = Alignment.End) {
            if (inFlight) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                OutlinedButton(onClick = onBook) {
                    Text(text = if (instance.status == ClassStatus.FULL) "Join Waitlist" else "Book")
                }
            }
            Text(
                text = "${instance.available} spots",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}

private fun ClassStatus.label(): String = when (this) {
    ClassStatus.CANCELLED -> "Cancelled"
    ClassStatus.FULL -> "Full"
    ClassStatus.AVAILABLE -> "Available"
    ClassStatus.UNKNOWN -> "Unavailable"
}

private const val DISABLED_ALPHA = 0.45f
