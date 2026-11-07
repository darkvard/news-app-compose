package com.example.news.di

import com.example.news.data.manager.LocalUserManagerImpl
import com.example.news.data.network.ConnectivityNetworkMonitor
import com.example.news.domain.manager.LocalUserManager
import com.example.news.domain.manager.NetworkMonitor
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
    abstract fun bindLocalUserManager(impl: LocalUserManagerImpl): LocalUserManager

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
