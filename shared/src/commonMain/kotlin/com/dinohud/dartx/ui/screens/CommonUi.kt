package com.dinohud.dartx.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dinohud.dartx.data.local.Match
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.action_back
import com.dinohud.dartx.resources.dartx_mark
import com.dinohud.dartx.resources.history_date
import com.dinohud.dartx.resources.in_double
import com.dinohud.dartx.resources.in_straight
import com.dinohud.dartx.resources.legs_count
import com.dinohud.dartx.resources.match_title_x01
import com.dinohud.dartx.resources.mode_cricket
import com.dinohud.dartx.resources.mode_split
import com.dinohud.dartx.resources.months_short
import com.dinohud.dartx.resources.out_double
import com.dinohud.dartx.resources.out_master
import com.dinohud.dartx.resources.out_single
import com.dinohud.dartx.resources.set_leg_best_of
import com.dinohud.dartx.resources.set_leg_first_to
import com.dinohud.dartx.resources.sets_count
import com.dinohud.dartx.resources.target_format
import com.dinohud.dartx.ui.theme.Green
import com.dinohud.dartx.ui.theme.GreenBright
import com.dinohud.dartx.ui.theme.OnGreen
import com.dinohud.dartx.ui.theme.Stroke
import com.dinohud.dartx.ui.theme.StrokeFaint
import com.dinohud.dartx.ui.theme.SurfaceCard
import com.dinohud.dartx.ui.theme.SurfaceInert
import com.dinohud.dartx.ui.theme.SurfaceRaised
import com.dinohud.dartx.ui.theme.TextFaint
import com.dinohud.dartx.ui.theme.TextInert
import com.dinohud.dartx.ui.theme.TextPrimary
import com.dinohud.dartx.ui.theme.TextSecondary
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

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
                    contentDescription = stringResource(Res.string.action_back),
                    tint = TextPrimary
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
internal fun SectionLabel(text: String, color: Color = TextSecondary) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color
    )
}

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
            painter = painterResource(Res.drawable.dartx_mark),
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

@Composable
internal fun outRuleLabel(rule: OutRule): String = stringResource(
    when (rule) {
        OutRule.DOUBLE_OUT -> Res.string.out_double
        OutRule.MASTER_OUT -> Res.string.out_master
        OutRule.SINGLE_OUT -> Res.string.out_single
    }
)

@Composable
internal fun inRuleLabel(rule: InRule): String = stringResource(
    when (rule) {
        InRule.STRAIGHT_IN -> Res.string.in_straight
        InRule.DOUBLE_IN -> Res.string.in_double
    }
)

@Composable
internal fun setLegModeLabel(mode: SetLegMode): String = stringResource(
    when (mode) {
        SetLegMode.FIRST_TO -> Res.string.set_leg_first_to
        SetLegMode.BEST_OF -> Res.string.set_leg_best_of
    }
)

@Composable
internal fun targetLabel(mode: SetLegMode, count: String): String =
    stringResource(Res.string.target_format, setLegModeLabel(mode).lowercase(), count)

@Composable
internal fun setsCount(sets: Int): String = pluralStringResource(Res.plurals.sets_count, sets, sets)

@Composable
internal fun legsCount(legs: Int): String = pluralStringResource(Res.plurals.legs_count, legs, legs)

@Composable
internal fun titleOf(match: Match): String = when (match.gameMode) {
    GameMode.X01 -> stringResource(Res.string.match_title_x01, match.startPoints, outRuleLabel(match.outRule))
    GameMode.CRICKET -> stringResource(Res.string.mode_cricket)
    GameMode.SPLIT -> stringResource(Res.string.mode_split)
}

@Composable
internal fun formatTimestamp(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val months = stringArrayResource(Res.array.months_short)
    val time = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    return stringResource(
        Res.string.history_date,
        dateTime.day,
        months[dateTime.month.ordinal],
        dateTime.year,
        time
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceSection(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
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
