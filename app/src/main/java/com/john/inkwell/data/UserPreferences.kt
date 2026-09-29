package com.john.inkwell.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "inkwell_prefs")

enum class FontPairing(val label: String) {
    CLASSIC_SERIF("Classic serif"),
    MODERN_SANS("Modern sans"),
    TYPEWRITER("Typewriter")
}

data class InkwellSettings(
    val accentIndex: Int = 0,
    val fontPairing: FontPairing = FontPairing.CLASSIC_SERIF,
    val grainIntensity: Float = 0.6f,
    val quickTags: Set<String> = setOf("work", "gratitude", "idea"),
    val quickMoods: Set<String> = setOf("calm", "tired", "excited", "anxious"),
    val lockEnabled: Boolean = false,
    val driveEmail: String? = null,
    val lastSyncedAt: Long? = null
)

class UserPreferences(private val context: Context) {

    private object Keys {
        val ACCENT_INDEX = intPreferencesKey("accent_index")
        val FONT_PAIRING = stringPreferencesKey("font_pairing")
        val GRAIN_INTENSITY = intPreferencesKey("grain_intensity_pct")
        val QUICK_TAGS = stringSetPreferencesKey("quick_tags")
        val QUICK_MOODS = stringSetPreferencesKey("quick_moods")
        val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
        val DRIVE_EMAIL = stringPreferencesKey("drive_email")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
    }

    val settings: Flow<InkwellSettings> = context.dataStore.data.map { prefs ->
        InkwellSettings(
            accentIndex = prefs[Keys.ACCENT_INDEX] ?: 0,
            fontPairing = prefs[Keys.FONT_PAIRING]?.let { name ->
                runCatching { FontPairing.valueOf(name) }.getOrNull()
            } ?: FontPairing.CLASSIC_SERIF,
            grainIntensity = (prefs[Keys.GRAIN_INTENSITY] ?: 60) / 100f,
            quickTags = prefs[Keys.QUICK_TAGS] ?: setOf("work", "gratitude", "idea"),
            quickMoods = prefs[Keys.QUICK_MOODS] ?: setOf("calm", "tired", "excited", "anxious"),
            lockEnabled = prefs[Keys.LOCK_ENABLED] ?: false,
            driveEmail = prefs[Keys.DRIVE_EMAIL],
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT]
        )
    }

    suspend fun setAccentIndex(index: Int) {
        context.dataStore.edit { it[Keys.ACCENT_INDEX] = index }
    }

    suspend fun setFontPairing(pairing: FontPairing) {
        context.dataStore.edit { it[Keys.FONT_PAIRING] = pairing.name }
    }

    suspend fun setGrainIntensity(fraction: Float) {
        context.dataStore.edit { it[Keys.GRAIN_INTENSITY] = (fraction * 100).toInt() }
    }

    suspend fun addQuickTag(tag: String) {
        context.dataStore.edit { it[Keys.QUICK_TAGS] = (it[Keys.QUICK_TAGS] ?: emptySet()) + tag }
    }

    suspend fun removeQuickTag(tag: String) {
        context.dataStore.edit { it[Keys.QUICK_TAGS] = (it[Keys.QUICK_TAGS] ?: emptySet()) - tag }
    }

    suspend fun addQuickMood(mood: String) {
        context.dataStore.edit { it[Keys.QUICK_MOODS] = (it[Keys.QUICK_MOODS] ?: emptySet()) + mood }
    }

    suspend fun removeQuickMood(mood: String) {
        context.dataStore.edit { it[Keys.QUICK_MOODS] = (it[Keys.QUICK_MOODS] ?: emptySet()) - mood }
    }

    suspend fun setLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCK_ENABLED] = enabled }
    }

    suspend fun setDriveEmail(email: String?) {
        context.dataStore.edit {
            if (email == null) it.remove(Keys.DRIVE_EMAIL) else it[Keys.DRIVE_EMAIL] = email
        }
    }

    suspend fun setLastSyncedAt(epochMillis: Long) {
        context.dataStore.edit { it[Keys.LAST_SYNCED_AT] = epochMillis }
    }
}
