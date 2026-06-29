package com.example.wearzone.di

import android.content.Context
import androidx.room.Room
import com.example.wearzone.data.db.WearZoneDatabase
import com.example.wearzone.data.local.search.RecentSearchDao
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
    fun provideRecentSearchDao(database: WearZoneDatabase): RecentSearchDao {
        return database.recentSearchDao()
    }
}
