package com.example.news.usecase.app_entry

import com.example.news.domain.manager.LocalUserManger
import kotlinx.coroutines.flow.Flow

class ReadAppEntry(
    private val localUserManager: LocalUserManger
) {
    operator fun invoke(): Flow<Boolean> {
        return localUserManager.readAppEntry()
    }
}