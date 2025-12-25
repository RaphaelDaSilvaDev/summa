package com.omna.summa.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.omna.summa.data.local.entity.ShoppingItemEntity
import com.omna.summa.data.local.entity.ShoppingListEntity
import java.time.LocalDate

data class ShoppingListWithItems(
    @Embedded
    val list: ShoppingListEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<ShoppingItemEntity>,
)
