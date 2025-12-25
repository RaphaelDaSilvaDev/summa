package com.omna.summa.domain.model

data class ShoppingItem(
    val id: Long = 0,
    var name: String,
    var quantity: Int = 0,
    var unit: String,
    var unitPrice: Double? = null
){
    fun totalPrice(): Double{
        return (unitPrice ?: 0.0) * quantity
    }
}
