package com.example.dartx.model.stats

data class PlayerMatchStats(
    val playerId: Long,
    val matchId: Long,
    val isWinner: Boolean,
    val legsPlayed: Int,
    val legsWon: Int,
    val totals: ScoringTotals
)

data class PlayerOverallStats(
    val playerId: Long,
    val matchesPlayed: Int,
    val matchesWon: Int,
    val legsPlayed: Int,
    val legsWon: Int,
    val totals: ScoringTotals
) {

    val winRate: Double? get() = if (matchesPlayed == 0) null else matchesWon.toDouble() / matchesPlayed

    val legWinRate: Double? get() = if (legsPlayed == 0) null else legsWon.toDouble() / legsPlayed

    companion object {
        fun aggregate(playerId: Long, perMatch: List<PlayerMatchStats>): PlayerOverallStats =
            PlayerOverallStats(
                playerId = playerId,
                matchesPlayed = perMatch.size,
                matchesWon = perMatch.count { it.isWinner },
                legsPlayed = perMatch.sumOf { it.legsPlayed },
                legsWon = perMatch.sumOf { it.legsWon },
                totals = perMatch.fold(ScoringTotals.EMPTY) { acc, match -> acc + match.totals }
            )
    }
}
