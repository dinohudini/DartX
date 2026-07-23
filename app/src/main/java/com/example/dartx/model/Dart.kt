package com.example.dartx.model

/** A single thrown dart. [fieldValue] is 1-20, or 25 for bull. */
data class Dart(val fieldValue: Int, val multiplier: Multiplier) {

    init {
        require(fieldValue in 1..20 || fieldValue == 25) { "Invalid field value: $fieldValue" }
        require(!(fieldValue == 25 && multiplier == Multiplier.TRIPLE)) { "Bull has no triple" }
    }

    val score: Int
        get() = when (multiplier) {
            Multiplier.SINGLE -> fieldValue
            Multiplier.DOUBLE -> fieldValue * 2
            Multiplier.TRIPLE -> fieldValue * 3
        }

    val isDouble: Boolean get() = multiplier == Multiplier.DOUBLE
    val isTriple: Boolean get() = multiplier == Multiplier.TRIPLE
}
