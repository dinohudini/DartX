package com.dinohud.dartx.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.match_stats_missing
import com.dinohud.dartx.resources.match_stats_title
import com.dinohud.dartx.resources.match_stats_unfinished_detail
import com.dinohud.dartx.resources.match_stats_unfinished_title
import com.dinohud.dartx.resources.player_deleted
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.MatchStatsViewModel
import com.dinohud.dartx.viewmodel.StatSectionStyle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MatchStatsScreen(
    matchId: Long,
    onBack: () -> Unit,
    viewModel: MatchStatsViewModel = koinViewModel { parametersOf(matchId) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(Res.string.match_stats_title), onBack = onBack) }
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
                    text = stringResource(Res.string.match_stats_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )

                !state.isCounted -> EmptyState(
                    title = stringResource(Res.string.match_stats_unfinished_title),
                    detail = stringResource(Res.string.match_stats_unfinished_detail),
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
                            PlayerHeading(
                                name = player.name ?: stringResource(Res.string.player_deleted),
                                avatarColor = player.avatarColor
                            )
                        }
                        items(
                            count = player.sections.size,
                            key = { index -> "section-${player.playerId}-$index" }
                        ) { index ->
                            val section = player.sections[index]
                            if (section.style == StatSectionStyle.BASKETS) {
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
