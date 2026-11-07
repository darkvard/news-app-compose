package com.example.news.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.news.data.local.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE feed = :feed")
    suspend fun get(feed: String): RemoteKeyEntity?

    @Upsert
    suspend fun upsert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE feed = :feed")
    suspend fun delete(feed: String)
}
