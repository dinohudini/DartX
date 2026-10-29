package com.dinohud.dartx.data.local

import androidx.room.TypeConverter
import com.dinohud.dartx.model.GameMode
import com.dinohud.dartx.model.InRule
import com.dinohud.dartx.model.Multiplier
import com.dinohud.dartx.model.OutRule
import com.dinohud.dartx.model.SetLegMode
import com.dinohud.dartx.model.ThrowStatus

class Converters {

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

    @TypeConverter
    fun fromThrowStatus(status: ThrowStatus): String = status.name

    @TypeConverter
    fun toThrowStatus(value: String): ThrowStatus = ThrowStatus.valueOf(value)
}
