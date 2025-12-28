package com.omna.summa.ui.converters

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun formatQuantity(value: Double): String{
    val formatter = DecimalFormat("#.##")
    return formatter.format(value)
}

fun formatCurrencyBR(value: Double): String{
    val localeBR = Locale("pt", "BR")
    val formatter = NumberFormat.getCurrencyInstance(localeBR)
    return formatter.format(value)
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