package com.omna.summa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: LocalDate = LocalDate.now(),
    val plannedAt: LocalDate?,
    val isActive: Boolean
)
