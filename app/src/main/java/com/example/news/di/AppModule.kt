package com.example.news.di

import android.app.Application
import androidx.room.Room
import com.example.news.data.local.NewsDao
import com.example.news.data.local.NewsDatabase
import com.example.news.data.local.NewsTypeConverter
import com.example.news.data.remote.NewsApi
import com.example.news.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): NewsApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        application: Application
    ): NewsDatabase {
        return Room.databaseBuilder(
            context = application, klass = NewsDatabase::class.java, name = "news_db"
        ).addTypeConverter(NewsTypeConverter())
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(newsDatabase: NewsDatabase): NewsDao {
        return newsDatabase.newsDao
    }
}