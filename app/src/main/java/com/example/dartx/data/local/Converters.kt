package com.example.dartx.data.local

import androidx.room.TypeConverter
import com.example.dartx.model.GameMode
import com.example.dartx.model.InRule
import com.example.dartx.model.Multiplier
import com.example.dartx.model.OutRule
import com.example.dartx.model.SetLegMode

class Converters {

    @TypeConverter
    fun fromParticipantIds(ids: List<Long>): String = ids.joinToString(",")

    @TypeConverter
    fun toParticipantIds(value: String): List<Long> =
        if (value.isEmpty()) emptyList() else value.split(",").map { it.toLong() }

    @TypeConverter
    fun fromScores(scores: List<Int>): String = scores.joinToString(",")

    @TypeConverter
    fun toScores(value: String): List<Int> =
        if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }

    @TypeConverter
    fun fromGameMode(mode: GameMode): String = mode.name

    @TypeConverter
    fun toGameMode(value: String): GameMode = GameMode.valueOf(value)

    @TypeConverter
    fun fromOutRule(rule: OutRule): String = rule.name

    @TypeConverter
    fun toOutRule(value: String): OutRule = OutRule.valueOf(value)

    @TypeConverter
    fun fromInRule(rule: InRule): String = rule.name

    @TypeConverter
    fun toInRule(value: String): InRule = InRule.valueOf(value)

    @TypeConverter
    fun fromSetLegMode(mode: SetLegMode): String = mode.name

    @TypeConverter
    fun toSetLegMode(value: String): SetLegMode = SetLegMode.valueOf(value)

    @TypeConverter
    fun fromMultiplier(multiplier: Multiplier?): String? = multiplier?.name

    @TypeConverter
    fun toMultiplier(value: String?): Multiplier? = value?.let { Multiplier.valueOf(it) }
}
