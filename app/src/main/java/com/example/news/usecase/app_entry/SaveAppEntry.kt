package com.example.news.usecase.app_entry

import com.example.news.domain.manager.LocalUserManger
import javax.inject.Inject

class SaveAppEntry @Inject constructor(
    private val localUserManager: LocalUserManger
) {
    suspend operator fun invoke() {
        localUserManager.saveAppEntry()
    }
}