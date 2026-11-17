package de.paso2.einkaufsliste.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) {
    SYSTEM("Systemstandard"),
    LIGHT("Hell"),
    DARK("Dunkel")
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val activeListIdKey = longPreferencesKey("active_list_id")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[themeKey]
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    val activeListId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[activeListIdKey]
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    suspend fun setActiveListId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[activeListIdKey] = id
        }
    }
}
