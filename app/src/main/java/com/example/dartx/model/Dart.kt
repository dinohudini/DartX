package com.example.dartx.model

/** A single thrown dart. [fieldValue] is 1-20, 25 for bull, or 0 for a complete miss. */
data class Dart(val fieldValue: Int, val multiplier: Multiplier = Multiplier.SINGLE) {

    init {
        require(fieldValue == 0 || fieldValue in 1..20 || fieldValue == 25) { "Invalid field value: $fieldValue" }
        require(!(fieldValue == 25 && multiplier == Multiplier.TRIPLE)) { "Bull has no triple" }
        require(!(fieldValue == 0 && multiplier != Multiplier.SINGLE)) { "A miss has no multiplier" }
    }

    val score: Int
        get() = when (multiplier) {
            Multiplier.SINGLE -> fieldValue
            Multiplier.DOUBLE -> fieldValue * 2
            Multiplier.TRIPLE -> fieldValue * 3
        }

    val isDouble: Boolean get() = multiplier == Multiplier.DOUBLE
    val isTriple: Boolean get() = multiplier == Multiplier.TRIPLE

    companion object {
        /** A dart that hit nothing — scores 0 and never satisfies an in/out rule. */
        val MISS = Dart(0, Multiplier.SINGLE)
    }
}
