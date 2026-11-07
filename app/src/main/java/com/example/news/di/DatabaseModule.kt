package com.example.news.di

import android.content.Context
import androidx.room.Room
import com.example.news.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Dev setup: no hand-written migrations. When an entity changes, bump the version and
    // Room drops & recreates the tables (or simply clear the app data).
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NewsDatabase =
        Room.databaseBuilder(context, NewsDatabase::class.java, NewsDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
}
