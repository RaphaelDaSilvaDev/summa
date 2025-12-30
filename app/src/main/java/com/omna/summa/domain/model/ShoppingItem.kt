package com.omna.summa.domain.model

data class ShoppingItem(
    val id: Long = 0,
    var name: String,
    var quantity: Double = 0.0,
    var unit: String,
    var unitPrice: Long? = null
){
    fun totalPriceInCents(): Long {
        return (quantity * (unitPrice ?: 0L)).toLong()
    }
}
