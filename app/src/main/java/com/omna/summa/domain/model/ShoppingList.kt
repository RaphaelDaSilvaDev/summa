package com.omna.summa.domain.model

import java.time.LocalDate

data class ShoppingList(
    val id: Long = 0,
    var name: String,
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: LocalDate = LocalDate.now(),
    var plannedAt: LocalDate? = null
){
    val itemCount: Int
        get() = items.size

    val totalPrice: Double
        get() = items.sumOf { it.totalPrice() }
}