package com.virginactive.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.virginactive.android.theme.Spacing
import com.virginactive.android.ui.common.ErrorState
import com.virginactive.android.ui.common.LoadingState
import com.virginactive.android.ui.home.render.ManifestBlockView
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onClassClick: (String) -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) { vm.load() }

    val state = vm.state

    when {
        state.isLoading -> LoadingState()

        state.error != null -> ErrorState(onRetry = vm::load)

        state.manifest == null || state.manifest.blocks.isEmpty() -> EmptyHome()

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            items(state.manifest.blocks) { block ->
                ManifestBlockView(block = block, onClassClick = onClassClick)
            }
        }
    }
}

@Composable
private fun EmptyHome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Nothing to show yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
