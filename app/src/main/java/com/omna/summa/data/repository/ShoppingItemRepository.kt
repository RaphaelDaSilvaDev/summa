package com.omna.summa.data.repository

import com.omna.summa.data.local.dao.ShoppingItemDao
import com.omna.summa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShoppingItemRepository @Inject constructor(
    private val dao: ShoppingItemDao
) {
    fun getItemsByList(listId: Long): Flow<List<ShoppingItemEntity>> = dao.getItemsByList(listId)

    fun getItemById(id: Long): ShoppingItemEntity? = dao.getItemById(id)

    suspend fun exists(id: Long): Boolean = dao.exists(id)

    suspend fun updateItem(item: ShoppingItemEntity) = dao.updateItem(item)

    suspend fun insetItem(item: ShoppingItemEntity): Long = dao.insertItem(item)

    suspend fun deleteItem(item: ShoppingItemEntity) = dao.deleteItem(item)
}