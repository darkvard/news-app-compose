package com.example.news.domain.manager

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    /** true when the device has a validated internet connection. */
    val isOnline: Flow<Boolean>
}
