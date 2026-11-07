package com.example.news.domain.usecase.app_entry

import com.example.news.domain.manager.LocalUserManager
import javax.inject.Inject

class SaveAppEntry @Inject constructor(
    private val localUserManager: LocalUserManager
) {
    suspend operator fun invoke() = localUserManager.saveAppEntry()
}
