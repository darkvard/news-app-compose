package com.example.news.di

import com.example.news.data.manager.LocalUserManagerImpl
import com.example.news.domain.manager.LocalUserManger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    @Singleton
    abstract fun bindLocalUserManager(localUserManagerImpl: LocalUserManagerImpl): LocalUserManger

}