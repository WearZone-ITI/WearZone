package com.example.wearzone.data.local.home

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_cache")
data class HomeCacheEntity(
    @PrimaryKey
    val key: String,
    val jsonContent: String
)
