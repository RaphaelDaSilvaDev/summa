package com.omna.summa.domain.model

import java.time.LocalDate

data class ShoppingList(
    val id: Long = 0,
    var name: String,
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: LocalDate = LocalDate.now(),
    var plannedAt: LocalDate? = null
){
    val totalPrice: Long
        get() = items.sumOf { it.totalPriceInCents() }
}