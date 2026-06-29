package com.example.wearzone.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WearZoneEntity::class],version = 1)
abstract class WearZoneDatabase : RoomDatabase() {

}