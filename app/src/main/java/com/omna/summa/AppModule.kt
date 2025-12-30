package com.omna.summa

import android.app.Application
import androidx.room.Room
import com.omna.summa.data.local.database.AppDatabase
import com.omna.summa.data.local.dao.ShoppingItemDao
import com.omna.summa.data.local.dao.ShoppingListDao
import com.omna.summa.data.local.database.migrations.MIGRATION_1_2
import com.omna.summa.data.local.database.migrations.MIGRATION_2_3
import com.omna.summa.data.repository.ShoppingItemRepository
import com.omna.summa.data.repository.ShoppingListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideeDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "summa_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideShoppingListDao(db: AppDatabase) = db.shoppingListDao()

    @Provides
    @Singleton
    fun provideShoppingListRepository(dao: ShoppingListDao) = ShoppingListRepository(dao)

    @Provides
    fun provideShoppingItemDao(db: AppDatabase) = db.shoppingItemDao()

    @Provides
    @Singleton
    fun provideShoppingItemRepository(dao: ShoppingItemDao) = ShoppingItemRepository(dao)
}