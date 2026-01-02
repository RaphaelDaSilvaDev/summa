package com.omna.summa.domain.model

data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String,
    val unitPrice: Long? = null,
    val isDone: Boolean = false
){
    fun totalPriceInCents(): Long {
        return (quantity * (unitPrice ?: 0L)).toLong()
    }
}
