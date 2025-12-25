package com.omna.summa.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE

        )
    ]
)
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long,
    var name: String,
    var quantity: Int,
    var unit: String,
    var unitPrice: Double?
)
