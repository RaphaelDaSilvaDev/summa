package com.omna.summa.data.local.mapper

import com.omna.summa.data.local.entity.ShoppingItemEntity
import com.omna.summa.domain.model.ShoppingItem

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = this.id,
    name = this.name,
    quantity = this.quantity,
    unit = this.unit,
    unitPrice = this.unitPrice
)

fun ShoppingItem.toEntry(listId: Long): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    listId = listId,
    name = name,
    quantity = quantity,
    unit = unit,
    unitPrice = unitPrice
)