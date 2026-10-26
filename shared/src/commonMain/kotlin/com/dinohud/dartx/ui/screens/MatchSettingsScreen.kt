package com.dinohud.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.settings_per_dart
import com.dinohud.dartx.resources.settings_per_dart_detail
import com.dinohud.dartx.resources.settings_rules
import com.dinohud.dartx.resources.settings_rules_detail
import com.dinohud.dartx.resources.settings_score_input
import com.dinohud.dartx.resources.settings_score_input_detail
import com.dinohud.dartx.resources.settings_title
import com.dinohud.dartx.resources.settings_turn_total
import com.dinohud.dartx.resources.settings_turn_total_detail
import com.dinohud.dartx.resources.setup_in_rule
import com.dinohud.dartx.resources.setup_legs
import com.dinohud.dartx.resources.setup_out_rule
import com.dinohud.dartx.resources.setup_sets
import com.dinohud.dartx.resources.setup_start_points
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.StrokeFaint
import com.dinohud.dartx.ui.theme.StrokeHairline
import com.dinohud.dartx.ui.theme.SurfaceCard
import com.dinohud.dartx.ui.theme.SurfaceRaised
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.ui.theme.TextSecondary
import com.dinohud.dartx.viewmodel.LiveMatchViewModel
import com.dinohud.dartx.viewmodel.ScoreInputMode
import org.jetbrains.compose.resources.stringResource

@Composable
fun MatchSettingsScreen(viewModel: LiveMatchViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar(title = stringResource(Res.string.settings_title), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            SettingsSection(
                title = stringResource(Res.string.settings_score_input),
                description = stringResource(Res.string.settings_score_input_detail)
            ) {
                inputModeOrder.forEachIndexed { index, option ->
                    if (index > 0) HorizontalDivider(color = StrokeFaint)
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
                    title = stringResource(Res.string.settings_rules),
                    description = stringResource(Res.string.settings_rules_detail)
                ) {
                    val rules = listOf(
                        stringResource(Res.string.setup_start_points) to match.startPoints.toString(),
                        stringResource(Res.string.setup_out_rule) to outRuleLabel(match.outRule),
                        stringResource(Res.string.setup_in_rule) to inRuleLabel(match.inRule),
                        stringResource(Res.string.setup_sets) to targetLabel(match.setLegMode, match.setsTarget.toString()),
                        stringResource(Res.string.setup_legs) to targetLabel(match.setLegMode, match.legsTarget.toString())
                    )
                    rules.forEachIndexed { index, (label, value) ->
                        if (index > 0) HorizontalDivider(color = StrokeHairline)
                        RuleRow(label = label, value = value)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .panelSurface()
        ) {
            content()
        }
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
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) SurfaceRaised else SurfaceCard)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(16.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Green,
                unselectedColor = TextFaint
            )
        )
        Column(
            modifier = Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) TextPrimary else TextSecondary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) TextSecondary else TextFaint
            )
        }
    }
}

@Composable
private fun RuleRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
    }
}

private val inputModeOrder = listOf(ScoreInputMode.PER_DART, ScoreInputMode.TURN_TOTAL)

@Composable
private fun inputModeTitle(mode: ScoreInputMode): String = stringResource(
    when (mode) {
        ScoreInputMode.TURN_TOTAL -> Res.string.settings_turn_total
        ScoreInputMode.PER_DART -> Res.string.settings_per_dart
    }
)

@Composable
private fun inputModeDescription(mode: ScoreInputMode): String = stringResource(
    when (mode) {
        ScoreInputMode.TURN_TOTAL -> Res.string.settings_turn_total_detail
        ScoreInputMode.PER_DART -> Res.string.settings_per_dart_detail
    }
)
