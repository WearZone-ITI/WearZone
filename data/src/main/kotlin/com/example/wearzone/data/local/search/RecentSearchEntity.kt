package com.example.wearzone.data.local.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    @ColumnInfo(name = "searched_at")
    val searchedAt: Long,
)
