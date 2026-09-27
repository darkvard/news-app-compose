package com.example.news.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news.usecase.app_entry.SaveAppEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveAppEntry: SaveAppEntry,
): ViewModel() {

    fun onEvent(event: OnboardingEvent) {
        when(event) {
            OnboardingEvent.SaveAppEntry -> {
                saveUserEntry()
            }
        }
    }

    private fun saveUserEntry() {
        viewModelScope.launch {
            saveAppEntry()
        }
    }
}