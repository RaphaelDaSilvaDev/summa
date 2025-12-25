package com.omna.summa.domain.model

import java.time.LocalDate

data class ShoppingList(
    val id: Long,
    val items: List<ShoppingItem>,
    val createdAt: LocalDate = LocalDate.now()
){
    val itemCount: Int
        get() = items.size

    val totalPrice: Double
        get() = items.sumOf { it.totalPrice() }
}