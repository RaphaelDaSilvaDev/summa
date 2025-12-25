package com.omna.summa.domain.model

import java.time.LocalDate

data class ShoppingList(
    val itemCount: Int = 0,
    val totalPrice: Double? = null,
    val items: List<ShoppingItem> = arrayListOf(),
    val createdAt: LocalDate = LocalDate.now()
)