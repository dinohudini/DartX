package com.example.dartx.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dartx.model.InRule
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = actions
    )
}

// Rule labels live here rather than on one screen, because setup picks the rules and the
// live match's settings screen shows them back.
internal fun outRuleLabel(rule: OutRule): String = when (rule) {
    OutRule.DOUBLE_OUT -> "Double out"
    OutRule.MASTER_OUT -> "Master out"
    OutRule.SINGLE_OUT -> "Single out"
}

internal fun inRuleLabel(rule: InRule): String = when (rule) {
    InRule.STRAIGHT_IN -> "Straight in"
    InRule.DOUBLE_IN -> "Double in"
}

internal fun setLegModeLabel(mode: SetLegMode): String = when (mode) {
    SetLegMode.FIRST_TO -> "First to"
    SetLegMode.BEST_OF -> "Best of"
}

/** A labelled group of single-choice chips, used throughout the match setup flow. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceSection(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    enabled: (T) -> Boolean = { true }
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    enabled = enabled(option),
                    label = { Text(label(option)) },
                    modifier = Modifier
                )
            }
        }
    }
}
