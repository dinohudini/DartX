package com.example.dartx.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Match
import com.example.dartx.model.Dart
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import com.example.dartx.ui.theme.Green
import com.example.dartx.ui.theme.GreenBright
import com.example.dartx.ui.theme.OnGreen
import com.example.dartx.ui.theme.Red
import com.example.dartx.ui.theme.RedBright
import com.example.dartx.ui.theme.ScoreActive
import com.example.dartx.ui.theme.Stroke
import com.example.dartx.ui.theme.StrokeFaint
import com.example.dartx.ui.theme.SurfaceCard
import com.example.dartx.ui.theme.SurfaceInert
import com.example.dartx.ui.theme.SurfaceRaised
import com.example.dartx.ui.theme.SurfaceRecessed
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextInert
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.LiveMatchViewModel
import com.example.dartx.viewmodel.LiveMatchViewModelFactory
import com.example.dartx.viewmodel.MessageTone
import com.example.dartx.viewmodel.PlayerBoard
import com.example.dartx.viewmodel.ScoreInputMode
import com.example.dartx.viewmodel.formatAverage

private val KeyShape = RoundedCornerShape(10.dp)
private val CardShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchId: Long,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewStats: () -> Unit,
    viewModel: LiveMatchViewModel = viewModel(
        factory = LiveMatchViewModelFactory(LocalContext.current, matchId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var typed by remember { mutableStateOf("") }
    var confirmAbandon by remember { mutableStateOf(false) }

    val requestExit = { if (state.winner == null) confirmAbandon = true else onExit() }

    BackHandler(enabled = state.winner == null) { confirmAbandon = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state.match?.let(::matchTitle) ?: "Match").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = requestExit) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Match settings",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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

                state.error != null -> Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RedBright,
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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

                        Spacer(modifier = Modifier.height(6.dp))

                        when (state.inputMode) {
                            ScoreInputMode.PER_DART -> TurnSlots(state.pendingDarts)
                            ScoreInputMode.TURN_TOTAL -> TurnTotalDisplay(
                                typed = typed,
                                remaining = state.boards.firstOrNull { it.isCurrentPlayer }?.remaining
                            )
                        }

                        MessageLine(message = state.message, tone = state.messageTone)
                    }

                    when (state.inputMode) {
                        ScoreInputMode.TURN_TOTAL -> TurnTotalPad(
                            typed = typed,
                            canUndo = state.canUndo,
                            onTyped = { typed = it },
                            onSubmit = {
                                viewModel.submitTurnTotal(it)
                                typed = ""
                            },
                            onUndo = viewModel::undo
                        )

                        ScoreInputMode.PER_DART -> PerDartPad(
                            canUndo = state.canUndo,
                            onDart = viewModel::addDart,
                            onUndo = viewModel::undo
                        )
                    }
                }
            }

            state.winner?.let { winner ->
                MatchOverDialog(
                    winnerName = winner.name,
                    winnerColor = winner.avatarColor,
                    boards = state.boards,
                    decidedBySets = state.match?.let {
                        it.setLegMode.winsNeeded(it.setsTarget) > 1
                    } == true,
                    onDone = onExit,
                    onUndo = viewModel::undo,
                    onViewStats = onViewStats
                )
            }

            if (confirmAbandon && state.winner == null) {
                AbandonMatchDialog(
                    onKeepPlaying = { confirmAbandon = false },
                    onAbandon = {
                        confirmAbandon = false
                        onExit()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScoreboardRow(board: PlayerBoard, displayedRemaining: Int, showInFlag: Boolean) {
    val active = board.isCurrentPlayer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(CardShape)
            .background(if (active) SurfaceRaised else SurfaceCard)
            .border(
                width = 1.dp,
                color = if (active) Green.copy(alpha = 0.45f) else StrokeFaint,
                shape = CardShape
            )
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(parseHexColor(board.player.avatarColor))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = board.player.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (active) TextPrimary else TextSecondary,
                    maxLines = 1
                )
                Text(
                    text = buildString {
                        append("Sets ${board.setWins} · Legs ${board.legWins}")
                        board.threeDartAverage?.let { append(" · Avg ${formatAverage(it)}") }
                    }.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) TextSecondary else TextFaint,
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (active) Tag(text = "To throw", color = GreenBright)
                    if (showInFlag && !board.isIn) Tag(text = "Not in", color = RedBright)
                }
            }

            Text(
                text = displayedRemaining.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = if (active) ScoreActive else TextFaint
            )
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/** Three slots standing in for the three darts of a turn, so nobody has to count them. */
@Composable
private fun TurnSlots(pendingDarts: List<Dart>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val dart = pendingDarts.getOrNull(index)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(KeyShape)
                    .background(if (dart != null) SurfaceRaised else SurfaceRecessed)
                    .border(
                        width = 1.dp,
                        color = if (dart != null) Stroke else StrokeFaint,
                        shape = KeyShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (dart != null) {
                    Text(
                        text = dartLabel(dart),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 28.sp,
                            lineHeight = 28.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Stroke)
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnTotalDisplay(typed: String, remaining: Int?) {
    val entered = typed.toIntOrNull()
    val leaves = remaining?.let { it - (entered ?: 0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(CardShape)
            .background(SurfaceCard)
            .border(1.dp, Stroke, CardShape)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Turn score".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextFaint
            )
            if (leaves != null && entered != null) {
                Text(
                    text = if (leaves < 0) "would bust" else "leaves $leaves",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (leaves < 0) RedBright else GreenBright
                )
            }
        }
        Text(
            text = typed.ifEmpty { "0" },
            style = MaterialTheme.typography.displayMedium,
            color = if (typed.isEmpty()) TextFaint else ScoreActive
        )
    }
}

@Composable
private fun MessageLine(message: String?, tone: MessageTone) {
    val color = when (tone) {
        MessageTone.ALERT -> RedBright
        MessageTone.GOOD -> GreenBright
        MessageTone.NEUTRAL -> TextSecondary
    }
    val background = when (tone) {
        MessageTone.ALERT -> Red.copy(alpha = 0.14f)
        MessageTone.GOOD -> Green.copy(alpha = 0.14f)
        MessageTone.NEUTRAL -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(KeyShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.orEmpty().uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

/** Fast entry: type the whole turn's score on a keypad. */
@Composable
private fun TurnTotalPad(
    typed: String,
    canUndo: Boolean,
    onTyped: (String) -> Unit,
    onSubmit: (Int) -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit ->
                    Key(label = digit, height = 60.dp, modifier = Modifier.weight(1f)) {
                        if (typed.length < 3) onTyped(typed + digit)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key(
                label = "CLEAR",
                numeric = false,
                height = 60.dp,
                tone = KeyTone.QUIET,
                modifier = Modifier.weight(1f)
            ) { onTyped("") }
            Key(label = "0", height = 60.dp, modifier = Modifier.weight(1f)) {
                if (typed.isNotEmpty() && typed.length < 3) onTyped(typed + "0")
            }
            Key(
                label = "ENTER",
                numeric = false,
                height = 60.dp,
                tone = KeyTone.PRIMARY,
                enabled = typed.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { onSubmit(typed.toIntOrNull() ?: 0) }
        }

        Key(
            label = "UNDO LAST TURN",
            numeric = false,
            tone = KeyTone.QUIET,
            enabled = canUndo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            onClick = onUndo
        )
    }
}

/** Detailed entry: pick the field that was hit, one dart at a time, with an optional multiplier. */
@Composable
private fun PerDartPad(
    canUndo: Boolean,
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit
) {
    var multiplier by remember { mutableStateOf(Multiplier.SINGLE) }
    val armed = multiplier != Multiplier.SINGLE

    fun throwDart(dart: Dart) {
        onDart(dart)
        // Multipliers apply to one dart only, so don't make the player unset it every time.
        multiplier = Multiplier.SINGLE
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..20).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { value ->
                    Key(label = value.toString(), armed = armed, modifier = Modifier.weight(1f)) {
                        throwDart(Dart(value, multiplier))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key(
                label = "25",
                armed = armed,
                // A bull has a single (25) and a double (50) ring, but no triple.
                enabled = multiplier != Multiplier.TRIPLE,
                modifier = Modifier.weight(1f)
            ) {
                throwDart(Dart(25, multiplier))
            }
            Key(label = "0", modifier = Modifier.weight(1f)) {
                throwDart(Dart.MISS)
            }
            Key(
                label = "UNDO",
                numeric = false,
                tone = KeyTone.QUIET,
                enabled = canUndo,
                modifier = Modifier.weight(1f),
                onClick = onUndo
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            listOf(Multiplier.DOUBLE, Multiplier.TRIPLE).forEach { option ->
                val selected = option == multiplier
                MultiplierChip(
                    label = multiplierLabel(option),
                    selected = selected,
                    modifier = Modifier.weight(1f)
                ) {
                    multiplier = if (selected) Multiplier.SINGLE else option
                }
            }
        }
    }
}

private enum class KeyTone { NEUTRAL, QUIET, PRIMARY }

/**
 * One key on either pad. Horizontal padding is deliberately tiny: Material's default content
 * padding is wide enough to wrap a two-digit label onto a second line on a narrow phone.
 */
@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = true,
    tone: KeyTone = KeyTone.NEUTRAL,
    armed: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> SurfaceInert
        tone == KeyTone.PRIMARY -> Green
        tone == KeyTone.QUIET -> SurfaceCard
        else -> SurfaceRaised
    }
    val outline = when {
        !enabled -> StrokeFaint
        tone == KeyTone.PRIMARY -> Green
        armed -> Green.copy(alpha = 0.5f)
        else -> Stroke
    }
    val content = when {
        !enabled -> TextInert
        tone == KeyTone.PRIMARY -> OnGreen
        tone == KeyTone.QUIET -> TextSecondary
        else -> TextPrimary
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(KeyShape)
            .background(background)
            .border(1.dp, outline, KeyShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = if (numeric) {
                MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp, lineHeight = 24.sp)
            } else {
                MaterialTheme.typography.labelLarge
            },
            color = content,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MultiplierChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(if (selected) Green.copy(alpha = 0.18f) else SurfaceCard)
            .border(1.dp, if (selected) Green else Stroke, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) GreenBright else TextSecondary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun AbandonMatchDialog(onKeepPlaying: () -> Unit, onAbandon: () -> Unit) {
    Dialog(onDismissRequest = onKeepPlaying) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceRaised)
                .border(1.dp, Stroke, RoundedCornerShape(24.dp))
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Abandon match".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = RedBright
                )
                Text(
                    text = "Leave now and the match stays unfinished.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Key(
                    label = "Keep playing",
                    numeric = false,
                    tone = KeyTone.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onKeepPlaying
                )
                Key(
                    label = "Abandon match",
                    numeric = false,
                    tone = KeyTone.QUIET,
                    height = 48.dp,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAbandon
                )
            }
        }
    }
}

@Composable
private fun MatchOverDialog(
    winnerName: String,
    winnerColor: String,
    boards: List<PlayerBoard>,
    decidedBySets: Boolean,
    onDone: () -> Unit,
    onUndo: () -> Unit,
    onViewStats: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceRaised)
                .border(1.dp, Stroke, RoundedCornerShape(24.dp))
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Match over".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = GreenBright
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(winnerColor))
                    )
                    Text(
                        text = "$winnerName wins",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    boards.forEachIndexed { index, board ->
                        if (index > 0) {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.displaySmall,
                                color = TextFaint
                            )
                        }
                        Text(
                            text = if (decidedBySets) {
                                board.setWins.toString()
                            } else {
                                board.totalLegWins.toString()
                            },
                            style = MaterialTheme.typography.displaySmall,
                            color = TextPrimary
                        )
                    }
                }
                Text(
                    text = (if (decidedBySets) "Sets" else "Legs").uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Key(
                    label = "Done",
                    numeric = false,
                    tone = KeyTone.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDone
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Key(
                        label = "Undo",
                        numeric = false,
                        tone = KeyTone.QUIET,
                        height = 48.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onUndo
                    )
                    Key(
                        label = "View stats",
                        numeric = false,
                        tone = KeyTone.QUIET,
                        height = 48.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onViewStats
                    )
                }
            }
        }
    }
}

private fun dartLabel(dart: Dart): String = when {
    dart.fieldValue == 0 -> "0"
    dart.multiplier == Multiplier.DOUBLE -> "D${dart.fieldValue}"
    dart.multiplier == Multiplier.TRIPLE -> "T${dart.fieldValue}"
    else -> dart.fieldValue.toString()
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
