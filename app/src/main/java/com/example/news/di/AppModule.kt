package com.example.news.di

import android.app.Application
import com.example.news.data.manager.LocalUserManagerImpl
import com.example.news.domain.manager.LocalUserManger
import com.example.news.usecase.app_entry.AppEntryUseCases
import com.example.news.usecase.app_entry.ReadAppEntry
import com.example.news.usecase.app_entry.SaveAppEntry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

}