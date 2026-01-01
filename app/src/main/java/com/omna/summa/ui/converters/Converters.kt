package com.omna.summa.ui.converters

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

fun formatQuantity(value: Double): String{
    val formatter = DecimalFormat("#.##")
    return formatter.format(value).toString().replace(".", ",")
}

fun formatCurrencyBR(cents: Long): String {
    val value = cents / 100.0
    return NumberFormat
        .getCurrencyInstance(Locale("pt", "BR"))
        .format(value)
}

fun parseCurrencyBRToCents(value: String): Long {
    if (value.isBlank()) return 0L

    val cleaned = value
        .replace("R$", "")
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()

    val doubleValue = cleaned.toDoubleOrNull() ?: return 0L

    return (doubleValue * 100).roundToLong()
}
fun formatPlannedDate(date: LocalDate): String{
    val today = LocalDate.now()
    return when (ChronoUnit.DAYS.between(today, date)){
        0L -> "Hoje"
        1L -> "Amanhã"
        in 2..6 -> "Em ${ChronoUnit.DAYS.between(today, date)} dias"
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }
}