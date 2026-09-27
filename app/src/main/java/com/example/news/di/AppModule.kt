package com.example.news.di

import android.app.Application
import androidx.room.Room
import com.example.news.data.local.NewsDao
import com.example.news.data.local.NewsDatabase
import com.example.news.data.local.NewsTypeConverter
import com.example.news.data.manager.LocalUserManagerImpl
import com.example.news.data.remote.NewsApi
import com.example.news.data.repository.NewsRepositoryImpl
import com.example.news.domain.manager.LocalUserManger
import com.example.news.domain.repository.NewsRepository
import com.example.news.usecase.app_entry.AppEntryUseCases
import com.example.news.usecase.app_entry.ReadAppEntry
import com.example.news.usecase.app_entry.SaveAppEntry
import com.example.news.usecase.news.DeleteArticle
import com.example.news.usecase.news.GetArticle
import com.example.news.usecase.news.GetArticles
import com.example.news.usecase.news.GetNews
import com.example.news.usecase.news.NewsUseCases
import com.example.news.usecase.news.SearchNews
import com.example.news.usecase.news.UpsertArticle
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
    fun provideLocalUserManager(application: Application): LocalUserManger {
        return LocalUserManagerImpl(application)
    }

    @Provides
    @Singleton
    fun provideAppEntryUseCases(localUserManager: LocalUserManger): AppEntryUseCases {
        return AppEntryUseCases(
            ReadAppEntry(localUserManager),
            SaveAppEntry(localUserManager)
        )
    }

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

    @Provides
    @Singleton
    fun provideNewsRepository(newsApi: NewsApi, newsDao: NewsDao): NewsRepository {
        return NewsRepositoryImpl(newsApi, newsDao)
    }

    @Provides
    @Singleton
    fun provideNewsUseCases(
        newsRepository: NewsRepository,
        newsDao: NewsDao
    ): NewsUseCases {
        return NewsUseCases(
            getNews = GetNews(newsRepository),
            searchNews = SearchNews(newsRepository),
            getArticle = GetArticle(newsDao),
            getArticles = GetArticles(newsDao),
            upsertArticle = UpsertArticle(newsDao),
            deleteArticle = DeleteArticle(newsDao)
        )
    }
}