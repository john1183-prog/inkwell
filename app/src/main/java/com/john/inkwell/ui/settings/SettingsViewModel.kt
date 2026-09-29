package com.john.inkwell.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.john.inkwell.data.FontPairing
import com.john.inkwell.data.InkwellSettings
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.data.drive.DriveSyncManager
import com.john.inkwell.data.drive.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Syncing : SyncUiState()
    data class Done(val message: String) : SyncUiState()
}

class SettingsViewModel(
    private val preferences: UserPreferences,
    private val driveSyncManager: DriveSyncManager
) : ViewModel() {

    val settings: StateFlow<InkwellSettings> = preferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InkwellSettings())

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState

    val signedInEmail: String?
        get() = driveSyncManager.lastSignedInAccount()?.email

    fun setAccentIndex(index: Int) = viewModelScope.launch { preferences.setAccentIndex(index) }
    fun setFontPairing(pairing: FontPairing) = viewModelScope.launch { preferences.setFontPairing(pairing) }
    fun setGrainIntensity(fraction: Float) = viewModelScope.launch { preferences.setGrainIntensity(fraction) }
    fun setLockEnabled(enabled: Boolean) = viewModelScope.launch { preferences.setLockEnabled(enabled) }

    fun addTag(tag: String) {
        if (tag.isBlank()) return
        viewModelScope.launch { preferences.addQuickTag(tag.trim()) }
    }
    fun removeTag(tag: String) = viewModelScope.launch { preferences.removeQuickTag(tag) }

    fun addMood(mood: String) {
        if (mood.isBlank()) return
        viewModelScope.launch { preferences.addQuickMood(mood.trim()) }
    }
    fun removeMood(mood: String) = viewModelScope.launch { preferences.removeQuickMood(mood) }

    fun onSignedIn(email: String?) {
        viewModelScope.launch { preferences.setDriveEmail(email) }
    }

    fun signOut() {
        viewModelScope.launch {
            driveSyncManager.signOut()
            preferences.setDriveEmail(null)
        }
    }

    fun sync() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing
            when (val result = driveSyncManager.sync()) {
                is SyncResult.Success -> {
                    preferences.setLastSyncedAt(System.currentTimeMillis())
                    _syncState.value = SyncUiState.Done("Synced — ${result.mergedCount} blocks total")
                }
                is SyncResult.Failure -> {
                    _syncState.value = SyncUiState.Done("Sync failed: ${result.message}")
                }
            }
        }
    }
}

class SettingsViewModelFactory(
    private val preferences: UserPreferences,
    private val driveSyncManager: DriveSyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(preferences, driveSyncManager) as T
    }
}
