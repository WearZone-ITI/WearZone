package com.example.wearzone.data.local.search

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wearzone.domain.search.model.RecentSearch

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey
    val query: String,
    @ColumnInfo(name = "searched_at")
    val searchedAt: Long,
) {
    fun toDomain(): RecentSearch = RecentSearch(query = query, searchedAt = searchedAt)
}

fun RecentSearch.toDto(): RecentSearchEntity = RecentSearchEntity(
    query = query,
    searchedAt = searchedAt,
)
