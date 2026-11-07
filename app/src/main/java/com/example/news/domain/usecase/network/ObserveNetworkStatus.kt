package com.example.news.domain.usecase.network

import com.example.news.domain.manager.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNetworkStatus @Inject constructor(
    private val networkMonitor: NetworkMonitor
) {
    /** Emits true while the device has a validated internet connection. */
    operator fun invoke(): Flow<Boolean> = networkMonitor.isOnline
}
