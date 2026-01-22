package com.omna.summa.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omna.summa.data.local.converters.Converters
import com.omna.summa.data.local.dao.ShoppingItemDao
import com.omna.summa.data.local.dao.ShoppingListDao
import com.omna.summa.data.local.dao.UserDao
import com.omna.summa.data.local.entity.ShoppingItemEntity
import com.omna.summa.data.local.entity.ShoppingListEntity
import com.omna.summa.data.local.entity.UserEntity

@Database(
    entities = [ShoppingListEntity::class, ShoppingItemEntity::class, UserEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun shoppingItemDao(): ShoppingItemDao

    abstract fun userDao(): UserDao
}