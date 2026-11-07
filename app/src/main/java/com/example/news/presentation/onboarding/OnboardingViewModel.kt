package com.example.news.presentation.onboarding

import androidx.lifecycle.viewModelScope
import com.example.news.domain.usecase.app_entry.SaveAppEntry
import com.example.news.presentation.base.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val saveAppEntry: SaveAppEntry,
) : MviViewModel<OnboardingState, OnboardingIntent, Nothing>(OnboardingState()) {

    override fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Finish -> viewModelScope.launch { saveAppEntry() }
        }
    }
}
