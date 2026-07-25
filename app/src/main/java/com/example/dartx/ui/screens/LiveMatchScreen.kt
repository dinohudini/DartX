package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Match
import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import com.example.dartx.viewmodel.LiveMatchViewModel
import com.example.dartx.viewmodel.LiveMatchViewModelFactory
import com.example.dartx.viewmodel.PlayerBoard
import com.example.dartx.viewmodel.ScoreInputMode

@Composable
fun LiveMatchScreen(
    matchId: Long,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LiveMatchViewModel = viewModel(
        factory = LiveMatchViewModelFactory(LocalContext.current, matchId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopBar(
                title = state.match?.let(::matchTitle) ?: "Match",
                onBack = onExit,
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Match settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.error != null -> Text(
                    text = state.error.orEmpty(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.boards.forEach { board ->
                            ScoreboardRow(
                                board = board,
                                displayedRemaining = if (board.isCurrentPlayer) {
                                    state.previewRemaining ?: board.remaining
                                } else {
                                    board.remaining
                                },
                                showInFlag = state.match?.inRule == InRule.DOUBLE_IN
                            )
                        }
                    }

                    Text(
                        text = state.message.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    when (state.inputMode) {
                        ScoreInputMode.TURN_TOTAL -> TurnTotalPad(onSubmit = viewModel::submitTurnTotal)
                        ScoreInputMode.PER_DART -> PerDartPad(
                            pendingDarts = state.pendingDarts,
                            onDart = viewModel::addDart,
                            onUndo = viewModel::undoPendingDart,
                            onEndTurn = viewModel::endTurnEarly
                        )
                    }
                }
            }

            state.winner?.let { winner ->
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Match over") },
                    text = { Text("${winner.name} wins!") },
                    confirmButton = {
                        TextButton(onClick = onExit) { Text("Done") }
                    }
                )
            }
        }
    }
}

@Composable
private fun ScoreboardRow(board: PlayerBoard, displayedRemaining: Int, showInFlag: Boolean) {
    Card(
        colors = if (board.isCurrentPlayer) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(board.player.avatarColor))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = board.player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (board.isCurrentPlayer) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "Sets ${board.setWins} · Legs ${board.legWins}" +
                        if (showInFlag && !board.isIn) " · not in" else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = displayedRemaining.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Fast entry: type the whole turn's score on a keypad. */
@Composable
private fun TurnTotalPad(onSubmit: (Int) -> Unit) {
    var typed by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = typed.ifEmpty { "0" },
                style = MaterialTheme.typography.headlineMedium
            )
        }

        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    PadButton(label = digit, modifier = Modifier.weight(1f)) {
                        if (typed.length < 3) typed += digit
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton(label = "C", modifier = Modifier.weight(1f)) { typed = "" }
            PadButton(label = "0", modifier = Modifier.weight(1f)) {
                if (typed.isNotEmpty() && typed.length < 3) typed += "0"
            }
            PadButton(label = "OK", modifier = Modifier.weight(1f)) {
                onSubmit(typed.toIntOrNull() ?: 0)
                typed = ""
            }
        }
    }
}

/** Detailed entry: pick a multiplier, then the field that was hit, one dart at a time. */
@Composable
private fun PerDartPad(
    pendingDarts: List<Dart>,
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit,
    onEndTurn: () -> Unit
) {
    var multiplier by remember { mutableStateOf(Multiplier.SINGLE) }

    fun throwDart(dart: Dart) {
        onDart(dart)
        // Multipliers apply to one dart only, so don't make the player unset it every time.
        multiplier = Multiplier.SINGLE
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "This turn: " +
                if (pendingDarts.isEmpty()) "—" else pendingDarts.joinToString(" ", transform = ::dartLabel),
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Multiplier.entries.forEach { option ->
                FilterChip(
                    selected = option == multiplier,
                    onClick = { multiplier = option },
                    label = { Text(multiplierLabel(option)) }
                )
            }
        }

        (1..20).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { value ->
                    PadButton(label = value.toString(), modifier = Modifier.weight(1f)) {
                        throwDart(Dart(value, multiplier))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PadButton(
                label = "Bull",
                modifier = Modifier.weight(1f),
                // A bull has a single (25) and a double (50) ring, but no triple.
                enabled = multiplier != Multiplier.TRIPLE
            ) {
                throwDart(Dart(25, multiplier))
            }
            PadButton(label = "Miss", modifier = Modifier.weight(1f)) { throwDart(Dart.MISS) }
            PadButton(
                label = "Undo",
                modifier = Modifier.weight(1f),
                enabled = pendingDarts.isNotEmpty(),
                onClick = onUndo
            )
        }

        Button(
            onClick = onEndTurn,
            enabled = pendingDarts.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End turn early")
        }
    }
}

@Composable
private fun PadButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp)
    ) {
        Text(label)
    }
}

private fun dartLabel(dart: Dart): String = when {
    dart.fieldValue == 0 -> "—"
    else -> "${multiplierLabel(dart.multiplier).first()}${dart.fieldValue}"
}

private fun multiplierLabel(multiplier: Multiplier): String = when (multiplier) {
    Multiplier.SINGLE -> "Single"
    Multiplier.DOUBLE -> "Double"
    Multiplier.TRIPLE -> "Triple"
}

private fun matchTitle(match: Match): String {
    val out = when (match.outRule) {
        OutRule.DOUBLE_OUT -> "double out"
        OutRule.MASTER_OUT -> "master out"
        OutRule.SINGLE_OUT -> "single out"
    }
    val mode = if (match.setLegMode == SetLegMode.FIRST_TO) "first to" else "best of"
    return "${match.startPoints} · $out · $mode ${match.legsTarget}"
}
