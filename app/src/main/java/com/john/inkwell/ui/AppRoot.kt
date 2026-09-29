package com.john.inkwell.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.john.inkwell.data.InkwellRepository
import com.john.inkwell.data.InkwellSettings
import com.john.inkwell.data.UserPreferences
import com.john.inkwell.data.drive.DriveSyncManager
import com.john.inkwell.lock.LockScreen
import com.john.inkwell.ui.navigation.InkwellNavHost
import com.john.inkwell.ui.theme.AccentPalette
import com.john.inkwell.ui.theme.InkwellTheme
import kotlinx.coroutines.flow.collect

@Composable
fun AppRoot(
    repository: InkwellRepository,
    preferences: UserPreferences,
    driveSyncManager: DriveSyncManager,
    onSignInClick: () -> Unit,
    onRequestUnlock: (onSuccess: () -> Unit) -> Unit
) {
    // Flow<InkwellSettings> is non-null, so collectAsState(initial = null) won't
    // type-check — produceState lets the initial value be null (still loading)
    // while every later emission is the real, non-null settings.
    val settingsState = produceState<InkwellSettings?>(initialValue = null, preferences) {
        preferences.settings.collect { value = it }
    }
    var unlocked by remember { mutableStateOf(false) }

    val currentSettings = settingsState.value
    if (currentSettings == null) {
        // First DataStore emission hasn't arrived yet — a blank paper
        // background avoids a flash of default (unthemed) content.
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val accent = AccentPalette.getOrElse(currentSettings.accentIndex) { AccentPalette.first() }

    InkwellTheme(accent = accent, fontPairing = currentSettings.fontPairing) {
        if (currentSettings.lockEnabled && !unlocked) {
            LockScreen(onUnlockTapped = { onRequestUnlock { unlocked = true } })
        } else {
            InkwellNavHost(
                repository = repository,
                preferences = preferences,
                driveSyncManager = driveSyncManager,
                onSignInClick = onSignInClick
            )
        }
    }
}
