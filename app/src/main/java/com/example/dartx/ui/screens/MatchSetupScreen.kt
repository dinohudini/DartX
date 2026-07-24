package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dartx.data.local.Player
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode
import com.example.dartx.viewmodel.MatchSetupViewModel
import com.example.dartx.viewmodel.MatchSetupViewModelFactory

private val startPointsPresets = listOf(501, 301)

/**
 * The setup flow from the spec as one ordered form: players -> game mode -> start points ->
 * out rule -> in rule -> sets/legs -> set/leg mode -> Start Game.
 */
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

    // Selection order is throw order, so the list is ordered, not a set.
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column {
                    Text(
                        text = "Players (${selectedIds.size} selected)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (players.isEmpty()) {
                        Text(
                            text = "No players yet — add some on the Players screen first.",
                            style = MaterialTheme.typography.bodySmall
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
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                ChoiceSection(
                    title = "Game mode",
                    options = GameMode.entries.toList(),
                    selected = gameMode,
                    label = ::gameModeLabel,
                    onSelect = { gameMode = it },
                    // Cricket and Split are phase 2; showing them disabled keeps the flow honest.
                    enabled = { it == GameMode.X01 }
                )

                Column {
                    Text(text = "Start points", style = MaterialTheme.typography.titleSmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        startPointsPresets.forEach { preset ->
                            OutlinedButton(onClick = { startPointsText = preset.toString() }) {
                                Text(preset.toString())
                            }
                        }
                        OutlinedTextField(
                            value = startPointsText,
                            onValueChange = { startPointsText = it.filter(Char::isDigit).take(4) },
                            label = { Text("Custom") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp)
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

                NumberStepper(title = "Sets", value = setsTarget, onValueChange = { setsTarget = it })
                NumberStepper(title = "Legs", value = legsTarget, onValueChange = { legsTarget = it })

                ChoiceSection(
                    title = "Set/leg mode",
                    options = SetLegMode.entries.toList(),
                    selected = setLegMode,
                    label = ::setLegModeLabel,
                    onSelect = { setLegMode = it }
                )

                Text(
                    text = summaryText(setLegMode, setsTarget, legsTarget),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
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
                },
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}

@Composable
private fun SelectablePlayerRow(player: Player, position: Int?, onToggle: () -> Unit) {
    val selected = position != null
    Card(
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
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
                    .background(parseHexColor(player.avatarColor))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            if (position != null) {
                Text(text = "#$position", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NumberStepper(title: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(80.dp)
        )
        OutlinedButton(onClick = { if (value > 1) onValueChange(value - 1) }) { Text("−") }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        OutlinedButton(onClick = { onValueChange(value + 1) }) { Text("+") }
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
    GameMode.CRICKET -> "Cricket (phase 2)"
    GameMode.SPLIT -> "Split (phase 2)"
}

private fun outRuleLabel(rule: OutRule): String = when (rule) {
    OutRule.DOUBLE_OUT -> "Double out"
    OutRule.MASTER_OUT -> "Master out"
    OutRule.SINGLE_OUT -> "Single out"
}

private fun inRuleLabel(rule: InRule): String = when (rule) {
    InRule.STRAIGHT_IN -> "Straight in"
    InRule.DOUBLE_IN -> "Double in"
}

private fun setLegModeLabel(mode: SetLegMode): String = when (mode) {
    SetLegMode.FIRST_TO -> "First to"
    SetLegMode.BEST_OF -> "Best of"
}
