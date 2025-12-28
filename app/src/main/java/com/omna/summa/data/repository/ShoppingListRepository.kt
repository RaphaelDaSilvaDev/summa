package com.omna.summa.data.repository

import com.omna.summa.data.local.dao.ShoppingListDao
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.relation.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val dao: ShoppingListDao
) {
    fun getAllLists(): Flow<List<ShoppingListWithItems>> = dao.getAllLists()

    suspend fun insertList(list: ShoppingListEntity): Long = dao.insertList(list)

    suspend fun deleteList(list: ShoppingListEntity) = dao.deleteList(list)

    suspend fun getListWithItems(listId: Long) = dao.getListWithItems(listId)

    suspend fun updateList(list: ShoppingListEntity) = dao.updateList(list)

    suspend fun getListById(listId: Long): ShoppingListEntity = dao.getListById(listId)
}