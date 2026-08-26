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
import com.example.dartx.ui.theme.Green
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.MatchStatsViewModel
import com.example.dartx.viewmodel.MatchStatsViewModelFactory

private const val BASKETS = "Scoring baskets"

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = "Match stats", onBack = onBack) }
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

                state.match == null -> Text(
                    text = "This match no longer exists.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )

                !state.isCounted -> EmptyState(
                    title = "Statistics are kept for finished matches only",
                    detail = "This match was abandoned before anyone won it.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        state.match?.let { match ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SectionLabel(titleOf(match))
                                Text(
                                    text = formatTimestamp(match.completedAt ?: match.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextFaint
                                )
                            }
                        }
                    }

                    state.players.forEach { player ->
                        item(key = "player-${player.playerId}") {
                            PlayerHeading(name = player.name, avatarColor = player.avatarColor)
                        }
                        items(
                            count = player.sections.size,
                            key = { index -> "section-${player.playerId}-$index" }
                        ) { index ->
                            val section = player.sections[index]
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
}
