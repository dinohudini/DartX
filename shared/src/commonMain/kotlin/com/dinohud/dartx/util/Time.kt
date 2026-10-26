package com.dinohud.dartx.util

import kotlin.time.Clock

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
