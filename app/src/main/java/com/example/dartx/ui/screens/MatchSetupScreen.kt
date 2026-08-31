package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Player
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import com.example.dartx.ui.theme.Green
import com.example.dartx.ui.theme.GreenBright
import com.example.dartx.ui.theme.OnGreen
import com.example.dartx.ui.theme.Stroke
import com.example.dartx.ui.theme.StrokeFaint
import com.example.dartx.ui.theme.SurfaceCard
import com.example.dartx.ui.theme.SurfaceRaised
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.ui.theme.TextSecondary
import com.example.dartx.viewmodel.MatchSetupViewModel
import com.example.dartx.viewmodel.MatchSetupViewModelFactory

private val startPointsPresets = listOf(501, 301)

@Composable
fun MatchSetupScreen(
    onBack: () -> Unit,
    onMatchCreated: (Long) -> Unit,
    viewModel: MatchSetupViewModel = viewModel(
        factory = MatchSetupViewModelFactory(LocalContext.current)
    )
) {
    val players by viewModel.players.collectAsStateWithLifecycle()
    val createdMatchId by viewModel.createdMatchId.collectAsStateWithLifecycle()

    var selectedIds by remember { mutableStateOf(emptyList<Long>()) }
    var gameMode by remember { mutableStateOf(GameMode.X01) }
    var startPointsText by remember { mutableStateOf("501") }
    var outRule by remember { mutableStateOf(OutRule.DOUBLE_OUT) }
    var inRule by remember { mutableStateOf(InRule.STRAIGHT_IN) }
    var setLegMode by remember { mutableStateOf(SetLegMode.FIRST_TO) }
    var setsTarget by remember { mutableStateOf(1) }
    var legsTarget by remember { mutableStateOf(3) }

    LaunchedEffect(createdMatchId) {
        createdMatchId?.let { id ->
            viewModel.onMatchOpened()
            onMatchCreated(id)
        }
    }

    val startPoints = startPointsText.toIntOrNull()
    val canStart = selectedIds.size >= 2 && startPoints != null && startPoints >= 2

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = "New match", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionLabel("Players")
                        SectionLabel(
                            text = "${selectedIds.size} selected",
                            color = if (selectedIds.size >= 2) GreenBright else TextFaint
                        )
                    }

                    if (players.isEmpty()) {
                        Text(
                            text = "No players yet — add some on the Players screen first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextFaint
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        players.forEach { player ->
                            SelectablePlayerRow(
                                player = player,
                                position = selectedIds.indexOf(player.id).takeIf { it >= 0 }?.plus(1),
                                onToggle = {
                                    selectedIds = if (player.id in selectedIds) {
                                        selectedIds - player.id
                                    } else {
                                        selectedIds + player.id
                                    }
                                }
                            )
                        }
                    }

                    Text(
                        text = "Order of selection is the throwing order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint
                    )
                }

                ChoiceSection(
                    title = "Game mode",
                    options = GameMode.entries.toList(),
                    selected = gameMode,
                    label = ::gameModeLabel,
                    onSelect = { gameMode = it },
                    enabled = { it == GameMode.X01 }
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("Start points")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        startPointsPresets.forEach { preset ->
                            PresetKey(
                                value = preset.toString(),
                                selected = startPointsText == preset.toString(),
                                onClick = { startPointsText = preset.toString() }
                            )
                        }
                        OutlinedTextField(
                            value = startPointsText,
                            onValueChange = { startPointsText = it.filter(Char::isDigit).take(4) },
                            label = { Text("Custom", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                            shape = ControlShape,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard,
                                focusedBorderColor = Green,
                                unfocusedBorderColor = Stroke,
                                cursorColor = Green,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = GreenBright,
                                unfocusedLabelColor = TextFaint
                            )
                        )
                    }
                }

                ChoiceSection(
                    title = "Out rule",
                    options = OutRule.entries.toList(),
                    selected = outRule,
                    label = ::outRuleLabel,
                    onSelect = { outRule = it }
                )

                ChoiceSection(
                    title = "In rule",
                    options = InRule.entries.toList(),
                    selected = inRule,
                    label = ::inRuleLabel,
                    onSelect = { inRule = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberStepper(
                        title = "Sets",
                        value = setsTarget,
                        onValueChange = { setsTarget = it },
                        modifier = Modifier.weight(1f)
                    )
                    NumberStepper(
                        title = "Legs",
                        value = legsTarget,
                        onValueChange = { legsTarget = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                ChoiceSection(
                    title = "Set / leg mode",
                    options = SetLegMode.entries.toList(),
                    selected = setLegMode,
                    label = ::setLegModeLabel,
                    onSelect = { setLegMode = it }
                )

                Text(
                    text = summaryText(setLegMode, setsTarget, legsTarget),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            HorizontalDivider(color = StrokeFaint)

            PrimaryButton(
                label = "START GAME",
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
            ) {
                startPoints?.let { points ->
                    viewModel.startMatch(
                        participantIds = selectedIds,
                        gameMode = gameMode,
                        startPoints = points,
                        outRule = outRule,
                        inRule = inRule,
                        setLegMode = setLegMode,
                        setsTarget = setsTarget,
                        legsTarget = legsTarget
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectablePlayerRow(player: Player, position: Int?, onToggle: () -> Unit) {
    val selected = position != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(ControlShape)
            .background(if (selected) SurfaceRaised else SurfaceCard)
            .border(1.dp, if (selected) Green else StrokeFaint, ControlShape)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarDot(colorHex = player.avatarColor, size = 26.dp)

        Text(
            text = player.name,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) TextPrimary else TextSecondary,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) Green else Color.Transparent)
                .border(1.dp, if (selected) Green else Stroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (position != null) {
                Text(
                    text = position.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 15.sp
                    ),
                    color = OnGreen
                )
            }
        }
    }
}

@Composable
private fun NumberStepper(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(ControlShape)
                .background(SurfaceCard)
                .border(1.dp, Stroke, ControlShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton(label = "−", enabled = value > 1) { onValueChange(value - 1) }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 24.sp
                ),
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            StepperButton(label = "+", enabled = true) { onValueChange(value + 1) }
        }
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(50.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) TextPrimary else TextFaint
        )
    }
}

private fun summaryText(mode: SetLegMode, sets: Int, legs: Int): String {
    val setsPart = "${setLegModeLabel(mode).lowercase()} $sets set(s)"
    val legsPart = "${setLegModeLabel(mode).lowercase()} $legs leg(s) per set"
    return "$setsPart, $legsPart — ${mode.winsNeeded(sets)} set win(s) and " +
        "${mode.winsNeeded(legs)} leg win(s) needed."
}

private fun gameModeLabel(mode: GameMode): String = when (mode) {
    GameMode.X01 -> "X01 (501/301)"
    GameMode.CRICKET -> "Cricket (coming soon)"
    GameMode.SPLIT -> "Split (coming soon)"
}

@Composable
private fun PresetKey(value: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(78.dp)
            .height(52.dp)
            .clip(ControlShape)
            .background(if (selected) Green.copy(alpha = 0.18f) else SurfaceCard)
            .border(1.dp, if (selected) Green else Stroke, ControlShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 24.sp
            ),
            color = if (selected) GreenBright else TextSecondary
        )
    }
}
