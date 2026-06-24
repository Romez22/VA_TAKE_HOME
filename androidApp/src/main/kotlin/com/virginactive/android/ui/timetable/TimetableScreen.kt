package com.virginactive.android.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.virginactive.android.theme.Spacing
import com.virginactive.android.ui.common.ErrorState
import com.virginactive.android.ui.common.LoadingState
import com.virginactive.shared.domain.home.DateTimeDisplay
import com.virginactive.shared.domain.timetable.TimetableDay
import org.koin.androidx.compose.koinViewModel

@Composable
fun TimetableScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    vm: TimetableViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) { vm.load() }

    val state = vm.state

    LaunchedEffect(state.snackbar) {
        val message = state.snackbar ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.snackbarShown()
    }

    Column(modifier = modifier.fillMaxSize()) {
        TimetableHeader()
        DayTabs(selected = state.selectedTab, onSelect = vm::selectTab)

        if (state.dateNotice != null) {
            DateNotice(text = state.dateNotice)
        }

        when {
            state.isLoading -> LoadingState()

            state.error != null -> ErrorState(onRetry = vm::load)

            else -> TimetableList(
                days = state.week?.daysFor(state.selectedTab).orEmpty(),
                rowActionInFlight = state.rowActionInFlight,
                onBook = vm::book,
            )
        }
    }
}

@Composable
private fun TimetableHeader() {
    Text(
        text = "Classes This Week",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            start = Spacing.md,
            end = Spacing.md,
            top = Spacing.lg,
            bottom = Spacing.sm,
        ),
    )
}

@Composable
private fun DateNotice(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimetableList(
    days: List<TimetableDay>,
    rowActionInFlight: Set<String>,
    onBook: (clubId: String, classId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.lg),
    ) {
        if (days.isEmpty()) {
            item { EmptyDay() }
            return@LazyColumn
        }

        days.forEach { day ->
            stickyHeader(key = "header-${day.date}") {
                DayHeader(date = day.date)
            }
            if (day.classes.isEmpty()) {
                item(key = "empty-${day.date}") { EmptyDay() }
            } else {
                items(items = day.classes, key = { it.classId }) { instance ->
                    ClassRow(
                        instance = instance,
                        inFlight = rowActionInFlight.contains(instance.classId),
                        onBook = { onBook(instance.clubId, instance.classId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: String) {
    Text(
        text = DateTimeDisplay.dateLabel(date),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    )
}

@Composable
private fun EmptyDay() {
    Text(
        text = "No classes this day",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.lg),
    )
}
