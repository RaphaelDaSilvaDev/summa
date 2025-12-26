package com.omna.summa.data.local.mapper

import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.relation.ShoppingListWithItems
import com.omna.summa.domain.model.ShoppingList

fun ShoppingListWithItems.toDomain(): ShoppingList = ShoppingList(
    id =  this.list.id,
    name = this.list.name,
    items = items.map { it.toDomain() },
    createdAt = this.list.createdAt
)

fun ShoppingList.toEntry(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    createdAt = createdAt
)