package com.omna.summa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.relation.ShoppingListWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity): Long

    @Delete
    suspend fun deleteList(list: ShoppingListEntity)

    @Query("SELECT * FROM shopping_lists ORDER BY id DESC")
    fun getAllLists(): Flow<List<ShoppingListWithItems>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :listId ORDER BY id DESC")
    suspend fun getListWithItems(listId: Long): ShoppingListWithItems?

    @Update
    suspend fun updateList(list: ShoppingListEntity)

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getListById(listId: Long): ShoppingListEntity
}