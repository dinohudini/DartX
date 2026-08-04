package com.example.dartx.viewmodel

import com.example.dartx.model.stats.ScoringTotals
import java.util.Locale

data class StatItem(val label: String, val value: String)

data class StatSection(val title: String, val items: List<StatItem>)

private const val NO_VALUE = "—"

internal fun formatAverage(value: Double): String = String.format(Locale.US, "%.2f", value)

internal fun formatRate(fraction: Double): String =
    String.format(Locale.US, "%.1f%%", fraction * 100)

internal fun scoringSections(totals: ScoringTotals): List<StatSection> = buildList {
    add(
        StatSection(
            title = "Scoring",
            items = listOf(
                StatItem("Total scored points", totals.pointsScored.toString()),
                StatItem("Darts thrown", totals.dartsThrown.toString()),
                StatItem("3-dart average", totals.threeDartAverage.formatted(::formatAverage)),
                StatItem("First 9 average", totals.first9Average.formatted(::formatAverage)),
                StatItem("Best leg average", totals.bestLegAverage.formatted(::formatAverage)),
                StatItem("Best leg (darts)", totals.bestLegDarts?.toString() ?: NO_VALUE),
                StatItem("Highest throw", totals.highestTurn.toString()),
                StatItem("Highest checkout", totals.highestCheckout?.toString() ?: NO_VALUE)
            )
        )
    )

    if (totals.dartsRecorded > 0) {
        add(
            StatSection(
                title = "Accuracy",
                items = listOf(
                    StatItem("20/19 hit rate", totals.hitRate20or19.formatted(::formatRate)),
                    StatItem("60/57 hit rate", totals.hitRateTriple20or19.formatted(::formatRate)),
                    StatItem("Triple rate", totals.tripleRate.formatted(::formatRate)),
                    StatItem("Dart 1 average", totals.dartAverage(0).formatted(::formatAverage)),
                    StatItem("Dart 2 average", totals.dartAverage(1).formatted(::formatAverage)),
                    StatItem("Dart 3 average", totals.dartAverage(2).formatted(::formatAverage))
                )
            )
        )
    }

    add(
        StatSection(
            title = "Scoring baskets",
            items = listOf(
                StatItem("180", totals.count180.toString()),
                StatItem("140+", totals.count140Plus.toString()),
                StatItem("100+", totals.count100Plus.toString()),
                StatItem("60+", totals.count60Plus.toString())
            )
        )
    )
}

private fun Double?.formatted(format: (Double) -> String): String = this?.let(format) ?: NO_VALUE
