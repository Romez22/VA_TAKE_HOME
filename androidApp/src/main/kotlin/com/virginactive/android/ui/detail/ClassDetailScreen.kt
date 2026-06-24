package com.virginactive.android.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.virginactive.android.theme.CardCornerRadius
import com.virginactive.android.theme.OnWarningContainer
import com.virginactive.android.theme.Spacing
import com.virginactive.android.theme.WarningBorder
import com.virginactive.android.theme.WarningContainer
import com.virginactive.android.ui.common.ErrorState
import com.virginactive.android.ui.common.LoadingState
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.booking.ClassStatus
import com.virginactive.shared.domain.booking.UserBookingStatus
import com.virginactive.shared.domain.home.DateTimeDisplay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    vm: ClassDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(classId) { vm.load(classId) }

    val state = vm.state

    LaunchedEffect(state.actionError) {
        if (state.actionError == null) return@LaunchedEffect
        snackbarHostState.showSnackbar("We couldn't complete that. Tap to try again.")
        vm.actionErrorShown()
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Class Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))

            state.error != null -> ErrorState(
                onRetry = { vm.load(classId) },
                modifier = Modifier.padding(innerPadding),
            )

            state.classInstance != null -> ClassDetailBody(
                instance = state.classInstance,
                confirmation = state.confirmation,
                actionInFlight = state.actionInFlight,
                withinForfeitWindow = vm.isWithinForfeitWindow(),
                onBook = vm::book,
                onCancel = vm::cancel,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ClassDetailBody(
    instance: ClassInstance,
    confirmation: Confirmation?,
    actionInFlight: Boolean,
    withinForfeitWindow: Boolean,
    onBook: () -> Unit,
    onCancel: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var showReminderSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var reminderMessage by remember { mutableStateOf<String?>(null) }
    var reminderAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(reminderMessage) {
        val message = reminderMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(message, actionLabel = "Open settings")
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) reminderAction?.invoke()
        reminderMessage = null
        reminderAction = null
    }

    val isBooked = confirmation?.userBookingStatus == UserBookingStatus.BOOKED ||
        confirmation?.userBookingStatus == UserBookingStatus.WAITLISTED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
    ) {
        TypeChip(type = instance.type.name)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = instance.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "with ${instance.trainer}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Spacer(Modifier.height(Spacing.md))
        InfoCard(instance = instance)

        if (confirmation != null) {
            Spacer(Modifier.height(Spacing.md))
            ConfirmationContent(confirmation = confirmation)
        }

        if (withinForfeitWindow && isBooked) {
            Spacer(Modifier.height(Spacing.md))
            ForfeitStrip()
        }

        Spacer(Modifier.height(Spacing.lg))
        PrimaryAction(
            status = instance.status,
            actionInFlight = actionInFlight,
            onBook = onBook,
        )

        if (isBooked) {
            Spacer(Modifier.height(Spacing.sm))
            Button(
                onClick = { showReminderSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("Set Reminder")
            }

            Spacer(Modifier.height(Spacing.sm))
            TextButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Cancel Booking", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showReminderSheet) {
        ReminderBottomSheet(
            classInstance = instance,
            onDismiss = { showReminderSheet = false },
            onSnackbar = { message, _, onAction ->
                reminderMessage = message
                reminderAction = onAction
            },
        )
    }

    if (showCancelDialog) {
        CancelDialog(
            withinForfeitWindow = withinForfeitWindow,
            onConfirm = {
                showCancelDialog = false
                onCancel()
            },
            onDismiss = { showCancelDialog = false },
        )
    }
}

@Composable
private fun TypeChip(type: String) {
    Text(
        text = type,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
}

@Composable
private fun InfoCard(instance: ClassInstance) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Spacing.md),
    ) {
        val offset = DateTimeDisplay.offsetLabel(instance.startsAt)
        InfoRow(label = "Date", value = DateTimeDisplay.dateLabel(instance.startsAt))
        InfoRow(
            label = "Time",
            value = buildString {
                append(DateTimeDisplay.timeLabel(instance.startsAt))
                append(" – ")
                append(DateTimeDisplay.timeLabel(instance.endsAt))
                if (offset.isNotEmpty()) append(" ($offset)")
            },
        )
        InfoRow(label = "Availability", value = "${instance.available} of ${instance.spots} spots")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ForfeitStrip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(WarningContainer)
            .border(width = 2.dp, color = WarningBorder, shape = RoundedCornerShape(CardCornerRadius))
            .padding(Spacing.md),
    ) {
        Text(
            text = "Cancelling within 12 hours of the class incurs a forfeit.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnWarningContainer,
        )
    }
}

@Composable
private fun PrimaryAction(
    status: ClassStatus,
    actionInFlight: Boolean,
    onBook: () -> Unit,
) {
    val label = if (status == ClassStatus.FULL) "Join Waitlist" else "Book Class"
    Button(
        onClick = onBook,
        enabled = !actionInFlight && status != ClassStatus.CANCELLED,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (actionInFlight) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(label)
        }
    }
}

@Composable
private fun CancelDialog(
    withinForfeitWindow: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel this booking?") },
        text = {
            Text(
                if (withinForfeitWindow) {
                    "This class starts within 12 hours, so cancelling now incurs a forfeit."
                } else {
                    "You can rebook later if spots are available."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Cancel Booking", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Booking") }
        },
    )
}
