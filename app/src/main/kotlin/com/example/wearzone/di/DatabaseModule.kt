package com.example.wearzone.di

import android.content.Context
import androidx.room.Room
import com.example.wearzone.data.db.WearZoneDatabase
import com.example.wearzone.data.local.dao.CartDao
import com.example.wearzone.data.local.search.RecentSearchDao
import com.example.wearzone.data.local.wishlist.WishlistDao
import com.example.wearzone.data.local.home.HomeCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "wear_zone_database"

    @Provides
    @Singleton
    fun provideWearZoneDatabase(
        @ApplicationContext context: Context,
    ): WearZoneDatabase {
        return Room.databaseBuilder(
            context,
            WearZoneDatabase::class.java,
            DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRecentSearchDao(database: WearZoneDatabase): RecentSearchDao {
        return database.recentSearchDao()
    }

    @Provides
    @Singleton
    fun provideCartDao(database: WearZoneDatabase): CartDao = database.cartDao()

    @Provides
    @Singleton
    fun provideWishlistDao(database: WearZoneDatabase): WishlistDao {
        return database.wishlistDao()
    }

    @Provides
    @Singleton
    fun provideHomeCacheDao(database: WearZoneDatabase): HomeCacheDao {
        return database.homeCacheDao()
    }
}
