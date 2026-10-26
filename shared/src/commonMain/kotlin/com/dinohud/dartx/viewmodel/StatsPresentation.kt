package com.dinohud.dartx.viewmodel

import com.dinohud.dartx.model.stats.ScoringTotals
import com.dinohud.dartx.resources.Res
import com.dinohud.dartx.resources.stat_best_leg_average
import com.dinohud.dartx.resources.stat_best_leg_darts
import com.dinohud.dartx.resources.stat_dart_average
import com.dinohud.dartx.resources.stat_darts_thrown
import com.dinohud.dartx.resources.stat_first_9_average
import com.dinohud.dartx.resources.stat_highest_checkout
import com.dinohud.dartx.resources.stat_highest_turn
import com.dinohud.dartx.resources.stat_hit_rate_20_19
import com.dinohud.dartx.resources.stat_hit_rate_60_57
import com.dinohud.dartx.resources.stat_points_scored
import com.dinohud.dartx.resources.stat_section_accuracy
import com.dinohud.dartx.resources.stat_section_baskets
import com.dinohud.dartx.resources.stat_section_scoring
import com.dinohud.dartx.resources.stat_three_dart_average
import com.dinohud.dartx.resources.stat_triple_rate
import com.dinohud.dartx.util.toFixed
import org.jetbrains.compose.resources.StringResource

data class StatItem(val label: UiText, val value: UiText, val highlight: Boolean = false)

enum class StatSectionStyle { LIST, BASKETS }

data class StatSection(
    val title: StringResource,
    val items: List<StatItem>,
    val style: StatSectionStyle = StatSectionStyle.LIST
)

private const val NO_VALUE = "—"

internal fun formatAverage(value: Double): String = value.toFixed(2)

internal fun formatRate(fraction: Double): String = "${(fraction * 100).toFixed(1)}%"

internal fun statItem(label: StringResource, value: String, vararg labelArgs: Any): StatItem =
    StatItem(uiText(label, *labelArgs), UiText.Plain(value))

internal fun Double?.orNoValue(format: (Double) -> String): String = this?.let(format) ?: NO_VALUE

internal fun Int?.orNoValue(): String = this?.toString() ?: NO_VALUE

internal fun scoringSections(totals: ScoringTotals): List<StatSection> = buildList {
    add(
        StatSection(
            title = Res.string.stat_section_scoring,
            items = listOf(
                statItem(Res.string.stat_points_scored, totals.pointsScored.toString()),
                statItem(Res.string.stat_darts_thrown, totals.dartsThrown.toString()),
                statItem(Res.string.stat_three_dart_average, totals.threeDartAverage.orNoValue(::formatAverage)),
                statItem(Res.string.stat_first_9_average, totals.first9Average.orNoValue(::formatAverage)),
                statItem(Res.string.stat_best_leg_average, totals.bestLegAverage.orNoValue(::formatAverage)),
                statItem(Res.string.stat_best_leg_darts, totals.bestLegDarts.orNoValue()),
                statItem(Res.string.stat_highest_turn, totals.highestTurn.toString()),
                statItem(Res.string.stat_highest_checkout, totals.highestCheckout.orNoValue())
            )
        )
    )

    if (totals.dartsRecorded > 0) {
        add(
            StatSection(
                title = Res.string.stat_section_accuracy,
                items = listOf(
                    statItem(Res.string.stat_hit_rate_20_19, totals.hitRate20or19.orNoValue(::formatRate)),
                    statItem(Res.string.stat_hit_rate_60_57, totals.hitRateTriple20or19.orNoValue(::formatRate)),
                    statItem(Res.string.stat_triple_rate, totals.tripleRate.orNoValue(::formatRate)),
                    statItem(Res.string.stat_dart_average, totals.dartAverage(0).orNoValue(::formatAverage), 1),
                    statItem(Res.string.stat_dart_average, totals.dartAverage(1).orNoValue(::formatAverage), 2),
                    statItem(Res.string.stat_dart_average, totals.dartAverage(2).orNoValue(::formatAverage), 3)
                )
            )
        )
    }

    add(
        StatSection(
            title = Res.string.stat_section_baskets,
            style = StatSectionStyle.BASKETS,
            items = listOf(
                StatItem(UiText.Plain("180"), UiText.Plain(totals.count180.toString()), highlight = totals.count180 > 0),
                StatItem(UiText.Plain("140+"), UiText.Plain(totals.count140Plus.toString())),
                StatItem(UiText.Plain("100+"), UiText.Plain(totals.count100Plus.toString())),
                StatItem(UiText.Plain("60+"), UiText.Plain(totals.count60Plus.toString()))
            )
        )
    )
}
