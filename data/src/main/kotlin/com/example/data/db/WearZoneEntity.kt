package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class WearZoneEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val price: Double,
    val thumbnail: String? = null
)