package com.example.dartx.model

enum class SetLegMode {
    FIRST_TO,
    BEST_OF;

    /** Wins required to clinch, given the mode's target [n] (e.g. "best of 5" -> 3). */
    fun winsNeeded(n: Int): Int = when (this) {
        FIRST_TO -> n
        BEST_OF -> (n / 2) + 1
    }
}
