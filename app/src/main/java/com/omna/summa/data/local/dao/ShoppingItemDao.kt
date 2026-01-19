package com.omna.summa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omna.summa.data.local.entity.ShoppingItemEntity
import com.omna.summa.domain.model.ShoppingItem
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

    @Query("SELECT DISTINCT * FROM shopping_items WHERE name LIKE :query || '%' LIMIT 10")
    fun getItemsLikeName(query: String): Flow<List<ShoppingItemEntity>>
}