package com.example.dartx.model

enum class SetLegMode {
    FIRST_TO,
    BEST_OF;

    fun winsNeeded(n: Int): Int = when (this) {
        FIRST_TO -> n
        BEST_OF -> (n / 2) + 1
    }
}
