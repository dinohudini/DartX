package com.dinohud.dartx.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals).toLong()
    val scaled = (abs(this) * factor).roundToLong()
    val whole = scaled / factor
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    if (decimals == 0) return "$sign$whole"
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}
