package com.example.news.di

import android.app.Application
import com.example.news.data.manager.LocalUserManagerImpl
import com.example.news.data.remote.NewsApi
import com.example.news.data.repository.NewsRepositoryImpl
import com.example.news.domain.manager.LocalUserManger
import com.example.news.domain.repository.NewsRepository
import com.example.news.usecase.app_entry.AppEntryUseCases
import com.example.news.usecase.app_entry.ReadAppEntry
import com.example.news.usecase.app_entry.SaveAppEntry
import com.example.news.usecase.news.GetNews
import com.example.news.usecase.news.NewsUseCases
import com.example.news.usecase.news.SearchNews
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
    fun provideNewsRepository(newsApi: NewsApi): NewsRepository {
        return NewsRepositoryImpl(newsApi)
    }

    @Provides
    @Singleton
    fun provideNewsUseCases(
        newsRepository: NewsRepository
    ): NewsUseCases {
        return NewsUseCases(
            getNews = GetNews(newsRepository),
            searchNews = SearchNews(newsRepository)
        )
    }
}