package com.example.dartx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dartx.R
import com.example.dartx.data.local.Match
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
import com.example.dartx.ui.theme.SurfaceInert
import com.example.dartx.ui.theme.SurfaceRaised
import com.example.dartx.ui.theme.TextFaint
import com.example.dartx.ui.theme.TextInert
import com.example.dartx.ui.theme.TextPrimary
import com.example.dartx.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal val PanelShape = RoundedCornerShape(14.dp)
internal val ControlShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

/** The small tracked-out caps that head every group of controls. */
@Composable
internal fun SectionLabel(text: String, color: Color = TextSecondary) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

/** The one card surface the whole app uses: flat fill, hairline border, shared radius. */
internal fun Modifier.panelSurface(
    shape: Shape = PanelShape,
    fill: Color = SurfaceCard,
    border: Color = StrokeFaint
): Modifier = this
    .clip(shape)
    .background(fill)
    .border(1.dp, border, shape)

@Composable
internal fun PrimaryButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(ControlShape)
            .background(if (enabled) Green else SurfaceInert)
            .border(1.dp, if (enabled) Green else StrokeFaint, ControlShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) OnGreen else TextInert,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
internal fun SecondaryButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingChevron: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(ControlShape)
            .background(SurfaceCard)
            .border(1.dp, Stroke, ControlShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) TextPrimary else TextInert,
            maxLines = 1
        )
        if (trailingChevron) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = TextFaint,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/** A single-choice pill. Selected reads as armed green, the same as the live pad's chips. */
@Composable
internal fun ChoicePill(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> SurfaceInert
        selected -> Green.copy(alpha = 0.18f)
        else -> SurfaceCard
    }
    val outline = when {
        !enabled -> StrokeFaint
        selected -> Green
        else -> Stroke
    }
    val content = when {
        !enabled -> TextInert
        selected -> GreenBright
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, outline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = content,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
internal fun AvatarDot(colorHex: String?, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colorHex?.let(::parseHexColor) ?: SurfaceRaised)
    )
}

/** Shown wherever a list has nothing in it yet — the mark, dimmed, and one honest line. */
@Composable
internal fun EmptyState(
    title: String,
    detail: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            tint = TextInert,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                textAlign = TextAlign.Center
            )
        }
    }
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

internal fun titleOf(match: Match): String = when (match.gameMode) {
    GameMode.X01 -> "${match.startPoints} · ${outRuleLabel(match.outRule)}"
    GameMode.CRICKET -> "Cricket"
    GameMode.SPLIT -> "Split"
}

private val matchDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

internal fun formatTimestamp(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(matchDateFormat)

/** A labelled group of single-choice pills, used throughout the match setup flow. */
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                ChoicePill(
                    label = label(option),
                    selected = option == selected,
                    enabled = enabled(option),
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}
