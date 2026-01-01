package com.omna.summa.data.local.mapper

import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.relation.ShoppingListWithItems
import com.omna.summa.domain.model.ShoppingList

fun ShoppingListWithItems.toDomain(): ShoppingList = ShoppingList(
    id =  this.list.id,
    name = this.list.name,
    items = items.map { it.toDomain() },
    createdAt = this.list.createdAt,
    plannedAt = this.list.plannedAt,
    isActive = this.list.isActive
)

fun ShoppingListEntity.toDomain(): ShoppingList = ShoppingList(
    id =  this.id,
    name = this.name,
    createdAt = this.createdAt,
    plannedAt = this.plannedAt,
    isActive = this.isActive
)

fun ShoppingList.toEntry(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    plannedAt = plannedAt,
    isActive = isActive
)