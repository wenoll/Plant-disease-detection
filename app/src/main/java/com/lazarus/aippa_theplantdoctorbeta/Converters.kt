package com.lazarus.aippa_theplantdoctorbeta

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromActivityType(value: ActivityType): String {
        return value.name
    }

    @TypeConverter
    fun toActivityType(value: String): ActivityType {
        return ActivityType.valueOf(value)
    }
} 