package com.omna.summa.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omna.summa.data.local.converters.Converters
import com.omna.summa.data.local.dao.ShoppingItemDao
import com.omna.summa.data.local.dao.ShoppingListDao
import com.omna.summa.data.local.entity.ShoppingItemEntity
import com.omna.summa.data.local.entity.ShoppingListEntity

@Database(
    entities = [ShoppingListEntity::class, ShoppingItemEntity::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingItemDao(): ShoppingItemDao
}