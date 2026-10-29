package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.model.Dart
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.Multiplier
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.abandon_confirm
import com.dinohud.dartx.resources.abandon_detail
import com.dinohud.dartx.resources.abandon_keep_playing
import com.dinohud.dartx.resources.abandon_title
import com.dinohud.dartx.resources.action_back
import com.dinohud.dartx.resources.key_clear
import com.dinohud.dartx.resources.key_enter
import com.dinohud.dartx.resources.key_undo
import com.dinohud.dartx.resources.key_undo_last_turn
import com.dinohud.dartx.resources.live_board_record
import com.dinohud.dartx.resources.live_board_record_avg
import com.dinohud.dartx.resources.live_leaves
import com.dinohud.dartx.resources.live_not_in
import com.dinohud.dartx.resources.live_settings
import com.dinohud.dartx.resources.live_title
import com.dinohud.dartx.resources.live_to_throw
import com.dinohud.dartx.resources.live_turn_score
import com.dinohud.dartx.resources.live_would_bust
import com.dinohud.dartx.resources.match_fallback_title
import com.dinohud.dartx.resources.match_over_done
import com.dinohud.dartx.resources.match_over_legs
import com.dinohud.dartx.resources.match_over_sets
import com.dinohud.dartx.resources.match_over_title
import com.dinohud.dartx.resources.match_over_undo
import com.dinohud.dartx.resources.match_over_view_stats
import com.dinohud.dartx.resources.match_over_winner
import com.dinohud.dartx.ui.KeepScreenOn
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.GreenBright
import com.dinohud.dartx.ui.theme.OnGreen
import com.dinohud.dartx.ui.theme.Red
import com.dinohud.dartx.ui.theme.RedBright
import com.dinohud.dartx.ui.theme.ScoreActive
import com.dinohud.dartx.ui.theme.Stroke
import com.dinohud.dartx.ui.theme.StrokeFaint
import com.dinohud.dartx.ui.theme.SurfaceCard
import com.dinohud.dartx.ui.theme.SurfaceInert
import com.dinohud.dartx.ui.theme.SurfaceRaised
import com.dinohud.dartx.ui.theme.SurfaceRecessed
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextInert
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.LiveMatchViewModel
import com.dinohud.dartx.viewmodel.MessageTone
import com.dinohud.dartx.viewmodel.PlayerBoard
import com.dinohud.dartx.viewmodel.ScoreInputMode
import com.dinohud.dartx.viewmodel.asString
import com.dinohud.dartx.viewmodel.formatAverage
import kotlin.math.floor
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val KeyShape = RoundedCornerShape(10.dp)
private val CardShape = RoundedCornerShape(14.dp)

private val BoardGap = 10.dp
private val StripGap = 8.dp
private val PadGap = 8.dp
private val PadBottomPadding = 12.dp
private val ZoneGap = 16.dp
private val EdgeFadeHeight = 24.dp
private val MinCardHeight = 80.dp
private val MaxCardHeight = 100.dp
private const val PadKeyRows = 5
private const val MinCardsInZone = 2

internal data class PadSizes(
    val slotHeight: Dp,
    val displayHeight: Dp,
    val messageHeight: Dp,
    val keyHeight: Dp
) {
    val height: Dp
        get() = keyHeight * PadKeyRows + PadGap * (PadKeyRows - 1) + PadBottomPadding

    fun stripHeight(perDart: Boolean): Dp =
        (if (perDart) slotHeight else displayHeight) + StripGap + messageHeight + StripGap + ZoneGap
}

private val PadLadder = listOf(
    PadSizes(60.dp, 74.dp, 44.dp, 56.dp),
    PadSizes(52.dp, 64.dp, 36.dp, 48.dp),
    PadSizes(46.dp, 58.dp, 30.dp, 42.dp)
)

internal data class LiveMatchMetrics(
    val pad: PadSizes,
    val cardHeight: Dp,
    val zoneHeight: Dp
)

internal fun cardSpan(count: Int, card: Dp): Dp = card * count + BoardGap * (count - 1)

private fun boardsBudget(available: Dp, pad: PadSizes, perDart: Boolean): Dp =
    available - pad.height - pad.stripHeight(perDart)

internal fun metricsFor(available: Dp, boardCount: Int, perDart: Boolean): LiveMatchMetrics {
    val pad = PadLadder.firstOrNull {
        boardsBudget(available, it, perDart) >= cardSpan(MinCardsInZone, MinCardHeight)
    } ?: PadLadder.last()

    val budget = boardsBudget(available, pad, perDart)
    val capacity = ((budget + BoardGap) / (MinCardHeight + BoardGap)).toInt().coerceAtLeast(MinCardsInZone)
    val shown = boardCount.coerceIn(1, capacity)
    val slots = shown.coerceAtLeast(MinCardsInZone)
    val fair = (budget - BoardGap * (slots - 1)) / slots
    val card = Dp(floor(fair.value)).coerceIn(MinCardHeight, MaxCardHeight)

    return LiveMatchMetrics(
        pad = pad,
        cardHeight = card,
        zoneHeight = cardSpan(shown, card).coerceAtMost(budget)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMatchScreen(
    matchId: Long,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
    onViewStats: () -> Unit,
    viewModel: LiveMatchViewModel = koinViewModel { parametersOf(matchId) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var typed by rememberSaveable { mutableStateOf("") }
    var confirmAbandon by remember { mutableStateOf(false) }

    val requestExit = { if (state.winner == null) confirmAbandon = true else onExit() }

    KeepScreenOn()

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.winner == null,
        onBackCompleted = { confirmAbandon = true }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state.match?.let { matchTitle(it) } ?: stringResource(Res.string.match_fallback_title)).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = requestExit) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(Res.string.live_settings),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val available = maxHeight

            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Green
                )

                state.error != null -> Text(
                    text = state.error?.asString().orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = RedBright,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                else -> {
                    val perDart = state.padMode == ScoreInputMode.PER_DART
                    val metrics = metricsFor(available, state.boards.size, perDart)
                    val boardScroll = rememberScrollState()

                    FollowCurrentPlayer(
                        scrollState = boardScroll,
                        currentIndex = state.boards.indexOfFirst { it.isCurrentPlayer },
                        boardHeight = metrics.cardHeight
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.height(metrics.zoneHeight)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(boardScroll)
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(BoardGap)
                            ) {
                                state.boards.forEach { board ->
                                    ScoreboardRow(
                                        board = board,
                                        displayedRemaining = if (board.isCurrentPlayer) {
                                            state.previewRemaining ?: board.remaining
                                        } else {
                                            board.remaining
                                        },
                                        showInFlag = state.match?.inRule == InRule.DOUBLE_IN,
                                        height = metrics.cardHeight
                                    )
                                }
                            }

                            if (boardScroll.canScrollBackward) {
                                EdgeFade(modifier = Modifier.align(Alignment.TopCenter), atTop = true)
                            }
                            if (boardScroll.canScrollForward) {
                                EdgeFade(modifier = Modifier.align(Alignment.BottomCenter), atTop = false)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(StripGap)
                        ) {
                            when (state.padMode) {
                                ScoreInputMode.PER_DART -> TurnSlots(
                                    pendingDarts = state.pendingDarts,
                                    height = metrics.pad.slotHeight
                                )

                                ScoreInputMode.TURN_TOTAL -> TurnTotalDisplay(
                                    typed = typed,
                                    remaining = state.boards.firstOrNull { it.isCurrentPlayer }?.remaining,
                                    height = metrics.pad.displayHeight
                                )
                            }

                            MessageLine(
                                message = state.message?.asString(),
                                tone = state.messageTone,
                                height = metrics.pad.messageHeight
                            )
                        }

                        Spacer(modifier = Modifier.height(StripGap))

                        when (state.padMode) {
                            ScoreInputMode.TURN_TOTAL -> TurnTotalPad(
                                typed = typed,
                                canUndo = state.canUndo,
                                keyHeight = metrics.pad.keyHeight,
                                onTyped = { typed = it },
                                onSubmit = {
                                    viewModel.submitTurnTotal(it)
                                    typed = ""
                                },
                                onUndo = viewModel::undo
                            )

                            ScoreInputMode.PER_DART -> PerDartPad(
                                canUndo = state.canUndo,
                                keyHeight = metrics.pad.keyHeight,
                                onDart = viewModel::addDart,
                                onUndo = viewModel::undo
                            )
                        }
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
                ConfirmDialog(
                    title = stringResource(Res.string.abandon_title),
                    detail = stringResource(Res.string.abandon_detail),
                    dismissLabel = stringResource(Res.string.abandon_keep_playing),
                    confirmLabel = stringResource(Res.string.abandon_confirm),
                    onDismiss = { confirmAbandon = false },
                    onConfirm = {
                        confirmAbandon = false
                        onExit()
                    }
                )
            }
        }
    }
}

@Composable
private fun FollowCurrentPlayer(scrollState: ScrollState, currentIndex: Int, boardHeight: Dp) {
    val density = LocalDensity.current
    val viewport = scrollState.viewportSize
    val furthest = scrollState.maxValue

    LaunchedEffect(currentIndex, boardHeight, viewport, furthest) {
        if (currentIndex < 0 || viewport == 0 || furthest == 0) return@LaunchedEffect

        val step = with(density) { (boardHeight + BoardGap).toPx() }
        val height = with(density) { boardHeight.toPx() }
        val top = (currentIndex * step).roundToInt()
        val bottom = top + height.roundToInt()

        val target = when {
            top < scrollState.value -> top
            bottom > scrollState.value + viewport -> bottom - viewport
            else -> return@LaunchedEffect
        }

        scrollState.animateScrollTo(target.coerceIn(0, furthest))
    }
}

@Composable
private fun ScoreboardRow(
    board: PlayerBoard,
    displayedRemaining: Int,
    showInFlag: Boolean,
    height: Dp
) {
    val active = board.isCurrentPlayer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
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
                    text = boardRecord(board).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) TextSecondary else TextFaint,
                    maxLines = 1
                )
                if (active || (showInFlag && !board.isIn)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (active) Tag(text = stringResource(Res.string.live_to_throw), color = GreenBright)
                        if (showInFlag && !board.isIn) Tag(text = stringResource(Res.string.live_not_in), color = RedBright)
                    }
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
private fun EdgeFade(modifier: Modifier, atTop: Boolean) {
    val background = MaterialTheme.colorScheme.background
    val colors = if (atTop) {
        listOf(background, Color.Transparent)
    } else {
        listOf(Color.Transparent, background)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(EdgeFadeHeight)
            .background(Brush.verticalGradient(colors))
    )
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

@Composable
private fun TurnSlots(pendingDarts: List<Dart>, height: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val dart = pendingDarts.getOrNull(index)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
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
private fun TurnTotalDisplay(typed: String, remaining: Int?, height: Dp) {
    val entered = typed.toIntOrNull()
    val leaves = remaining?.let { it - (entered ?: 0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CardShape)
            .background(SurfaceCard)
            .border(1.dp, Stroke, CardShape)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.live_turn_score).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextFaint
            )
            if (leaves != null && entered != null) {
                Text(
                    text = if (leaves < 0) stringResource(Res.string.live_would_bust) else stringResource(Res.string.live_leaves, leaves),
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
private fun MessageLine(message: String?, tone: MessageTone, height: Dp) {
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
            .height(height)
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

@Composable
private fun TurnTotalPad(
    typed: String,
    canUndo: Boolean,
    keyHeight: Dp,
    onTyped: (String) -> Unit,
    onSubmit: (Int) -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = PadBottomPadding),
        verticalArrangement = Arrangement.spacedBy(PadGap)
    ) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(PadGap)) {
                row.forEach { digit ->
                    Key(label = digit, height = keyHeight, modifier = Modifier.weight(1f)) {
                        onTyped(appendDigit(typed, digit))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(PadGap)) {
            Key(
                label = stringResource(Res.string.key_clear),
                numeric = false,
                height = keyHeight,
                tone = KeyTone.QUIET,
                modifier = Modifier.weight(1f)
            ) { onTyped("") }
            Key(label = "0", height = keyHeight, modifier = Modifier.weight(1f)) {
                onTyped(appendDigit(typed, "0"))
            }
            Key(
                label = stringResource(Res.string.key_enter),
                numeric = false,
                height = keyHeight,
                tone = KeyTone.PRIMARY,
                enabled = typed.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) { onSubmit(typed.toIntOrNull() ?: 0) }
        }

        Key(
            label = stringResource(Res.string.key_undo_last_turn),
            numeric = false,
            height = keyHeight,
            tone = KeyTone.QUIET,
            enabled = canUndo,
            modifier = Modifier.fillMaxWidth(),
            onClick = onUndo
        )
    }
}

@Composable
private fun PerDartPad(
    canUndo: Boolean,
    keyHeight: Dp,
    onDart: (Dart) -> Unit,
    onUndo: () -> Unit
) {
    var multiplier by remember { mutableStateOf(Multiplier.SINGLE) }
    val armed = multiplier != Multiplier.SINGLE

    fun throwDart(dart: Dart) {
        onDart(dart)
        multiplier = Multiplier.SINGLE
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = PadBottomPadding),
        verticalArrangement = Arrangement.spacedBy(PadGap)
    ) {
        (1..20).chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(PadGap)) {
                row.forEach { value ->
                    Key(
                        label = value.toString(),
                        armed = armed,
                        height = keyHeight,
                        modifier = Modifier.weight(1f)
                    ) {
                        throwDart(Dart(value, multiplier))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(PadGap)) {
            Key(
                label = "25",
                armed = armed,
                enabled = multiplier != Multiplier.TRIPLE,
                height = keyHeight,
                modifier = Modifier.weight(1f)
            ) {
                throwDart(Dart(25, multiplier))
            }
            Key(label = "0", height = keyHeight, modifier = Modifier.weight(1f)) {
                throwDart(Dart.MISS)
            }
            Key(
                label = stringResource(Res.string.key_undo),
                numeric = false,
                tone = KeyTone.QUIET,
                enabled = canUndo,
                height = keyHeight,
                modifier = Modifier.weight(1f),
                onClick = onUndo
            )
            listOf(Multiplier.DOUBLE, Multiplier.TRIPLE).forEach { option ->
                val selected = option == multiplier
                MultiplierKey(
                    label = multiplierLabel(option),
                    selected = selected,
                    height = keyHeight,
                    modifier = Modifier.weight(1f)
                ) {
                    multiplier = if (selected) Multiplier.SINGLE else option
                }
            }
        }
    }
}

internal enum class KeyTone { NEUTRAL, QUIET, PRIMARY }

@Composable
internal fun Key(
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
private fun MultiplierKey(
    label: String,
    selected: Boolean,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(KeyShape)
            .background(if (selected) Green.copy(alpha = 0.18f) else SurfaceCard)
            .border(1.dp, if (selected) Green else Stroke, KeyShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp, lineHeight = 24.sp),
            color = if (selected) GreenBright else TextSecondary,
            maxLines = 1,
            softWrap = false
        )
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
                    text = stringResource(Res.string.match_over_title).uppercase(),
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
                        text = stringResource(Res.string.match_over_winner, winnerName),
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
                    text = stringResource(if (decidedBySets) Res.string.match_over_sets else Res.string.match_over_legs).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Key(
                    label = stringResource(Res.string.match_over_done),
                    numeric = false,
                    tone = KeyTone.PRIMARY,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDone
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Key(
                        label = stringResource(Res.string.match_over_undo),
                        numeric = false,
                        tone = KeyTone.QUIET,
                        height = 48.dp,
                        modifier = Modifier.weight(1f),
                        onClick = onUndo
                    )
                    Key(
                        label = stringResource(Res.string.match_over_view_stats),
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

internal fun appendDigit(typed: String, digit: String): String = when {
    typed == "0" -> digit
    typed.length < 3 -> typed + digit
    else -> typed
}

private fun dartLabel(dart: Dart): String = when {
    dart.fieldValue == 0 -> "0"
    dart.multiplier == Multiplier.DOUBLE -> "D${dart.fieldValue}"
    dart.multiplier == Multiplier.TRIPLE -> "T${dart.fieldValue}"
    else -> dart.fieldValue.toString()
}

private fun multiplierLabel(multiplier: Multiplier): String = when (multiplier) {
    Multiplier.SINGLE -> "×1"
    Multiplier.DOUBLE -> "×2"
    Multiplier.TRIPLE -> "×3"
}

@Composable
private fun matchTitle(match: Match): String = stringResource(
    Res.string.live_title,
    match.startPoints,
    outRuleLabel(match.outRule),
    setLegModeLabel(match.setLegMode),
    match.legsTarget
)

@Composable
private fun boardRecord(board: PlayerBoard): String {
    val average = board.threeDartAverage
        ?: return stringResource(Res.string.live_board_record, board.setWins, board.legWins)
    return stringResource(Res.string.live_board_record_avg, board.setWins, board.legWins, formatAverage(average))
}
