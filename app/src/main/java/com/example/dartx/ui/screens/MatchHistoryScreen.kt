package com.example.dartx.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Match
import com.example.dartx.ui.theme.Green
import com.example.dartx.ui.theme.Red
import com.example.dartx.ui.theme.RedBright
import com.example.dartx.ui.theme.StrokeFaint
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.HistoryParticipant
import com.example.dartx.viewmodel.MatchHistoryEntry
import com.example.dartx.viewmodel.MatchHistoryViewModel
import com.example.dartx.viewmodel.MatchHistoryViewModelFactory

@Composable
fun MatchHistoryScreen(
    onBack: () -> Unit,
    onOpenStats: (Long) -> Unit,
    viewModel: MatchHistoryViewModel = viewModel(
        factory = MatchHistoryViewModelFactory(LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = "Match history", onBack = onBack) }
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
                    title = "No matches played yet",
                    detail = "Finish a match and it lands here.",
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
    // Sets are only worth reporting when the match was actually played over more than one.
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
            text = participant.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (won) TextPrimary else TextSecondary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            // An unfinished match recorded no score, and a dash says that without claiming a 0.
            text = score?.toString() ?: "–",
            style = MaterialTheme.typography.displaySmall,
            color = if (won) TextPrimary else TextFaint
        )
    }
}

private fun subtitleOf(
    match: Match,
    decidedBySets: Boolean,
    isFinished: Boolean,
    participants: List<HistoryParticipant>
): String {
    if (!isFinished) return "Abandoned mid-match"

    val mode = setLegModeLabel(match.setLegMode)
    val format = if (decidedBySets) {
        "${mode.lowercase()} ${match.setsTarget} sets · ${mode.lowercase()} ${match.legsTarget} legs"
    } else {
        "${mode.lowercase()} ${match.legsTarget} legs"
    }
    val winner = participants.firstOrNull { it.isWinner }?.name
    return if (winner != null) "$winner won · $format" else format
}
