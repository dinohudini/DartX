package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dartx.viewmodel.LiveMatchViewModel
import com.example.dartx.viewmodel.ScoreInputMode

/**
 * Settings for the match that is currently being played, opened from the live match's top bar.
 *
 * It shares [LiveMatchViewModel] with the live screen (see the nav host), so a change here applies
 * to the match already in progress instead of only to the next one.
 */
@Composable
fun MatchSettingsScreen(viewModel: LiveMatchViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BackTopBar(title = "Match settings", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsSection(
                title = "Score input",
                description = "How darts are entered on the live screen."
            ) {
                ScoreInputMode.entries.forEachIndexed { index, option ->
                    if (index > 0) HorizontalDivider()
                    RadioOptionRow(
                        title = inputModeTitle(option),
                        description = inputModeDescription(option),
                        selected = option == state.inputMode,
                        onSelect = { viewModel.setInputMode(option) }
                    )
                }
            }

            state.match?.let { match ->
                SettingsSection(
                    title = "Match rules",
                    description = "Chosen at setup and fixed for the whole match."
                ) {
                    RuleRow("Start points", match.startPoints.toString())
                    RuleRow("Out rule", outRuleLabel(match.outRule))
                    RuleRow("In rule", inRuleLabel(match.inRule))
                    RuleRow(
                        "Sets",
                        "${setLegModeLabel(match.setLegMode).lowercase()} ${match.setsTarget}"
                    )
                    RuleRow(
                        "Legs",
                        "${setLegModeLabel(match.setLegMode).lowercase()} ${match.legsTarget}"
                    )
                }
            }
        }
    }
}

/** A titled block of related settings; new option groups slot in as further calls of this. */
@Composable
private fun SettingsSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (description != null) {
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
        Card(modifier = Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun RadioOptionRow(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // Selecting on the whole row, not just the button, is the expected touch target.
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(16.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RuleRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun inputModeTitle(mode: ScoreInputMode): String = when (mode) {
    ScoreInputMode.TURN_TOTAL -> "Turn total"
    ScoreInputMode.PER_DART -> "Per dart"
}

private fun inputModeDescription(mode: ScoreInputMode): String = when (mode) {
    ScoreInputMode.TURN_TOTAL -> "Fast: type the whole turn's score."
    ScoreInputMode.PER_DART -> "Detailed: every dart's field, for full statistics."
}
