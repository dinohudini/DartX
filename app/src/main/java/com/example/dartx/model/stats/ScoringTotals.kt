package com.example.dartx.model.stats

const val DART_POSITIONS = 3

data class ScoringTotals(
    val pointsScored: Int = 0,
    val dartsThrown: Int = 0,
    val first9Points: Int = 0,
    val first9Darts: Int = 0,
    val highestTurn: Int = 0,
    val highestCheckout: Int? = null,
    val bestLegAverage: Double? = null,
    val bestLegDarts: Int? = null,
    val count60Plus: Int = 0,
    val count100Plus: Int = 0,
    val count140Plus: Int = 0,
    val count180: Int = 0,
    val dartsRecorded: Int = 0,
    val dartPoints: List<Int> = List(DART_POSITIONS) { 0 },
    val dartCounts: List<Int> = List(DART_POSITIONS) { 0 },
    val hits20or19: Int = 0,
    val hitsTriple20or19: Int = 0,
    val hitsTriple: Int = 0
) {

    val threeDartAverage: Double? get() = ratio(pointsScored * 3.0, dartsThrown)

    val first9Average: Double? get() = ratio(first9Points * 3.0, first9Darts)

    val hitRate20or19: Double? get() = ratio(hits20or19.toDouble(), dartsRecorded)

    val hitRateTriple20or19: Double? get() = ratio(hitsTriple20or19.toDouble(), dartsRecorded)

    val tripleRate: Double? get() = ratio(hitsTriple.toDouble(), dartsRecorded)

    fun dartAverage(position: Int): Double? =
        ratio(dartPoints[position].toDouble(), dartCounts[position])

    operator fun plus(other: ScoringTotals): ScoringTotals = ScoringTotals(
        pointsScored = pointsScored + other.pointsScored,
        dartsThrown = dartsThrown + other.dartsThrown,
        first9Points = first9Points + other.first9Points,
        first9Darts = first9Darts + other.first9Darts,
        highestTurn = maxOf(highestTurn, other.highestTurn),
        highestCheckout = listOfNotNull(highestCheckout, other.highestCheckout).maxOrNull(),
        bestLegAverage = listOfNotNull(bestLegAverage, other.bestLegAverage).maxOrNull(),
        bestLegDarts = listOfNotNull(bestLegDarts, other.bestLegDarts).minOrNull(),
        count60Plus = count60Plus + other.count60Plus,
        count100Plus = count100Plus + other.count100Plus,
        count140Plus = count140Plus + other.count140Plus,
        count180 = count180 + other.count180,
        dartsRecorded = dartsRecorded + other.dartsRecorded,
        dartPoints = dartPoints.zip(other.dartPoints, Int::plus),
        dartCounts = dartCounts.zip(other.dartCounts, Int::plus),
        hits20or19 = hits20or19 + other.hits20or19,
        hitsTriple20or19 = hitsTriple20or19 + other.hitsTriple20or19,
        hitsTriple = hitsTriple + other.hitsTriple
    )

    companion object {
        val EMPTY = ScoringTotals()
    }
}

private fun ratio(numerator: Double, denominator: Int): Double? =
    if (denominator == 0) null else numerator / denominator
