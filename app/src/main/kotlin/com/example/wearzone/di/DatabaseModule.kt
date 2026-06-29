package com.example.wearzone.di

import android.content.Context
import androidx.room.Room
import com.example.wearzone.data.db.WearZoneDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "wear_zone_database"

    @Provides
    fun provideWearZoneDatabase(
        @ApplicationContext context: Context
    ): WearZoneDatabase {
        return Room.databaseBuilder(
            context, WearZoneDatabase::class.java, DATABASE_NAME
        ).build()
    }
}