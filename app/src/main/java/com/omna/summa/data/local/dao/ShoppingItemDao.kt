package com.omna.summa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omna.summa.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items WHERE listId = :listId")
    fun getItemsByList(listId: Long): Flow<List<ShoppingItemEntity>>
}