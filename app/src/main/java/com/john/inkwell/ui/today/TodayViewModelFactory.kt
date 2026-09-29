package com.john.inkwell.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.UserPreferences

class TodayViewModelFactory(
    private val repository: InkwellRepository,
    private val preferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TodayViewModel(repository, preferences) as T
    }
}
