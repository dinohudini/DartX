package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Match
import com.example.dartx.model.GameMode
import com.example.dartx.viewmodel.HistoryParticipant
import com.example.dartx.viewmodel.MatchHistoryEntry
import com.example.dartx.viewmodel.MatchHistoryViewModel
import com.example.dartx.viewmodel.MatchHistoryViewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MatchHistoryScreen(
    onBack: () -> Unit,
    viewModel: MatchHistoryViewModel = viewModel(
        factory = MatchHistoryViewModelFactory(LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BackTopBar(title = "Match history", onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.entries.isEmpty() -> Text(
                    text = "No matches played yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.entries, key = { it.match.id }) { entry ->
                        MatchHistoryCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchHistoryCard(entry: MatchHistoryEntry) {
    val match = entry.match
    // Sets are only worth reporting when the match was actually played over more than one.
    val decidedBySets = match.setLegMode.winsNeeded(match.setsTarget) > 1

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titleOf(match), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatTimestamp(match.completedAt ?: match.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            entry.participants.forEach { participant ->
                ParticipantRow(
                    participant = participant,
                    score = if (decidedBySets) participant.setWins else participant.legWins
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = subtitleOf(match, decidedBySets, entry.isFinished),
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.isFinished) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun ParticipantRow(participant: HistoryParticipant, score: Int?) {
    val weight = if (participant.isWinner) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    participant.avatarColor
                        ?.let { parseHexColor(it) }
                        ?: MaterialTheme.colorScheme.surfaceVariant
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = participant.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = weight,
            modifier = Modifier.weight(1f)
        )
        Text(
            // An unfinished match recorded no score, and a dash says that without claiming a 0.
            text = score?.toString() ?: "–",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = weight
        )
    }
}

private fun titleOf(match: Match): String = when (match.gameMode) {
    GameMode.X01 -> "${match.startPoints} · ${outRuleLabel(match.outRule)}"
    GameMode.CRICKET -> "Cricket"
    GameMode.SPLIT -> "Split"
}

private fun subtitleOf(match: Match, decidedBySets: Boolean, isFinished: Boolean): String {
    if (!isFinished) return "Unfinished"

    val mode = setLegModeLabel(match.setLegMode)
    val legs = "${mode.lowercase()} ${match.legsTarget} legs"
    return if (decidedBySets) "$mode ${match.setsTarget} sets · $legs" else "$mode ${match.legsTarget} legs"
}

private val historyDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

private fun formatTimestamp(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(historyDateFormat)
