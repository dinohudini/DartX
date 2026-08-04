package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.dartx.viewmodel.MatchStatsViewModel
import com.example.dartx.viewmodel.MatchStatsViewModelFactory

@Composable
fun MatchStatsScreen(
    matchId: Long,
    onBack: () -> Unit,
    viewModel: MatchStatsViewModel = viewModel(
        factory = MatchStatsViewModelFactory(LocalContext.current, matchId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BackTopBar(title = "Match stats", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.match == null -> Text(
                    text = "This match no longer exists.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )

                !state.isCounted -> Text(
                    text = "Statistics are kept for finished matches only.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        state.match?.let { match ->
                            Column {
                                Text(
                                    text = titleOf(match),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = formatTimestamp(match.completedAt ?: match.createdAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    state.players.forEach { player ->
                        item(key = "player-${player.playerId}") {
                            PlayerHeading(name = player.name, avatarColor = player.avatarColor)
                        }
                        items(player.sections.size) { index ->
                            StatSectionCard(player.sections[index])
                        }
                    }
                }
            }
        }
    }
}
