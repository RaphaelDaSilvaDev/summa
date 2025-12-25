package com.omna.summa.data.local.converters

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.format(formatter)

    @TypeConverter
    fun toLocalDate(date: String): LocalDate = LocalDate.parse(date, formatter)
}