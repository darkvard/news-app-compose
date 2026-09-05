package com.example.news.usecase.app_entry

import com.example.news.domain.manager.LocalUserManger

class SaveAppEntry(
    private val localUserManager: LocalUserManger
) {
    suspend operator fun invoke() {
        localUserManager.saveAppEntry()
    }
}