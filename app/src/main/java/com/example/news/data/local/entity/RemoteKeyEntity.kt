package com.example.news.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Next API page to request for a cached feed (null = end reached). */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val feed: String,
    val nextPage: Int?
)
