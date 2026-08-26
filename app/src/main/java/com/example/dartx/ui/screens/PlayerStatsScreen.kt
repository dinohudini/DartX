package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.ui.theme.Green
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.PlayerStatsViewModel
import com.example.dartx.viewmodel.PlayerStatsViewModelFactory

private const val BASKETS = "Scoring baskets"

@Composable
fun PlayerStatsScreen(
    playerId: Long,
    onBack: () -> Unit,
    viewModel: PlayerStatsViewModel = viewModel(
        factory = PlayerStatsViewModelFactory(LocalContext.current, playerId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = state.name ?: "Statistics", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Green
                )

                state.name == null -> Text(
                    text = "This player no longer exists.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )

                !state.hasPlayed -> EmptyState(
                    title = "No finished matches yet",
                    detail = "Statistics start counting once a match is won.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PlayerHeading(
                            name = state.name.orEmpty(),
                            avatarColor = state.avatarColor
                        )
                    }
                    items(state.sections) { section ->
                        if (section.title == BASKETS) {
                            BasketRow(section)
                        } else {
                            StatSectionCard(section)
                        }
                    }
                }
            }
        }
    }
}
