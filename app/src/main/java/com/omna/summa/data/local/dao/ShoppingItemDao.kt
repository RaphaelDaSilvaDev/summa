package com.omna.summa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omna.summa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM shopping_items WHERE id = :id)")
    suspend fun exists(id: Long): Boolean

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isDone, id")
    fun getItemsByList(listId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    fun getItemById(id: Long): ShoppingItemEntity?
}