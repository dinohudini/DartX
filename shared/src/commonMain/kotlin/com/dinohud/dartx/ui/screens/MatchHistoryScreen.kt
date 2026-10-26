package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.history_abandoned
import com.dinohud.dartx.resources.history_empty_detail
import com.dinohud.dartx.resources.history_empty_title
import com.dinohud.dartx.resources.history_format_sets
import com.dinohud.dartx.resources.history_title
import com.dinohud.dartx.resources.history_winner
import com.dinohud.dartx.resources.player_deleted
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.Red
import com.dinohud.dartx.ui.theme.RedBright
import com.dinohud.dartx.ui.theme.StrokeFaint
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.HistoryParticipant
import com.dinohud.dartx.viewmodel.MatchHistoryEntry
import com.dinohud.dartx.viewmodel.MatchHistoryViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MatchHistoryScreen(
    onBack: () -> Unit,
    onOpenStats: (Long) -> Unit,
    viewModel: MatchHistoryViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(Res.string.history_title), onBack = onBack) }
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

                state.entries.isEmpty() -> EmptyState(
                    title = stringResource(Res.string.history_empty_title),
                    detail = stringResource(Res.string.history_empty_detail),
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.entries, key = { it.match.id }) { entry ->
                        MatchHistoryCard(entry, onOpenStats = { onOpenStats(entry.match.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchHistoryCard(entry: MatchHistoryEntry, onOpenStats: () -> Unit) {
    val match = entry.match
    val decidedBySets = match.setLegMode.winsNeeded(match.setsTarget) > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .panelSurface(
                border = if (entry.isFinished) StrokeFaint else Red.copy(alpha = 0.35f)
            )
            .then(if (entry.isFinished) Modifier.clickable(onClick = onOpenStats) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(titleOf(match))
            Text(
                text = formatTimestamp(match.completedAt ?: match.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            entry.participants.forEach { participant ->
                ParticipantRow(
                    participant = participant,
                    score = if (decidedBySets) participant.setWins else participant.legWins
                )
            }
        }

        Text(
            text = subtitleOf(match, decidedBySets, entry.isFinished, entry.participants).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (entry.isFinished) TextFaint else RedBright
        )
    }
}

@Composable
private fun ParticipantRow(participant: HistoryParticipant, score: Int?) {
    val won = participant.isWinner

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarDot(colorHex = participant.avatarColor, size = 14.dp)
        Text(
            text = participant.name ?: stringResource(Res.string.player_deleted),
            style = MaterialTheme.typography.titleMedium,
            color = if (won) TextPrimary else TextSecondary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = score?.toString() ?: "–",
            style = MaterialTheme.typography.displaySmall,
            color = if (won) TextPrimary else TextFaint
        )
    }
}

@Composable
private fun subtitleOf(
    match: Match,
    decidedBySets: Boolean,
    isFinished: Boolean,
    participants: List<HistoryParticipant>
): String {
    if (!isFinished) return stringResource(Res.string.history_abandoned)

    val legs = targetLabel(match.setLegMode, legsCount(match.legsTarget))
    val format = if (decidedBySets) {
        stringResource(Res.string.history_format_sets, targetLabel(match.setLegMode, setsCount(match.setsTarget)), legs)
    } else {
        legs
    }
    val winner = participants.firstOrNull { it.isWinner } ?: return format
    val winnerName = winner.name ?: stringResource(Res.string.player_deleted)
    return stringResource(Res.string.history_winner, winnerName, format)
}
