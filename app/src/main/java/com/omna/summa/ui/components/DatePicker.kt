package com.omna.summa.ui.components

import android.app.DatePickerDialog
import android.content.Context
import java.time.LocalDate

fun showDatePicker(context: Context, initialDate: LocalDate = LocalDate.now(), onDateSelected: (LocalDate) -> Unit){
    val dialog = DatePickerDialog(
        context,
        {_, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day))},
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    )

    dialog.show()
}