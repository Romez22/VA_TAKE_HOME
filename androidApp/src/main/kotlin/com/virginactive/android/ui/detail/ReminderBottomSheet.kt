package com.virginactive.android.ui.detail

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.virginactive.android.reminder.CalendarInsert
import com.virginactive.android.reminder.ReminderScheduler
import com.virginactive.android.theme.Spacing
import com.virginactive.shared.domain.booking.ClassInstance
import com.virginactive.shared.domain.home.DateTimeDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    classInstance: ClassInstance,
    onDismiss: () -> Unit,
    onSnackbar: (message: String, actionLabel: String?, onAction: (() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val openSettings: () -> Unit = remember(context) {
        {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(intent) }
        }
    }

    val timeLabel = DateTimeDisplay.timeLabel(classInstance.startsAt)

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            ReminderScheduler.schedule(
                context = context,
                classId = classInstance.classId,
                title = classInstance.title,
                startsAtIso = classInstance.startsAt,
                timeLabel = timeLabel,
            )
            onDismiss()
        } else {
            onSnackbar("Reminder needs notification permission", "Open settings", openSettings)
            onDismiss()
        }
    }

    val onNotificationReminder: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ReminderScheduler.schedule(
                context = context,
                classId = classInstance.classId,
                title = classInstance.title,
                startsAtIso = classInstance.startsAt,
                timeLabel = timeLabel,
            )
            onDismiss()
        }
    }

    val onAddToCalendar: () -> Unit = {
        val launched = CalendarInsert.addToCalendar(
            context = context,
            title = classInstance.title,
            startsAtIso = classInstance.startsAt,
            endsAtIso = classInstance.endsAt,
            location = classInstance.clubId,
        )
        if (!launched) {
            onSnackbar("Calendar access was denied", "Open settings", openSettings)
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            Text(
                text = "Set a reminder",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )
            SheetOption(label = "Notification reminder", onClick = onNotificationReminder)
            SheetOption(label = "Add to calendar", onClick = onAddToCalendar)
        }
    }
}

@Composable
private fun SheetOption(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
    )
}
