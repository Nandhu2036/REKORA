package com.rekora.app.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK_AMOLED
}

@Singleton
class RekoraPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
        val KEY_DEV_MODE = booleanPreferencesKey("dev_mode")
        val KEY_SEED_DATA_LOADED = booleanPreferencesKey("seed_data_loaded")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userRole: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE] ?: "STUDENT"
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val modeStr = prefs[KEY_THEME_MODE]
        if (modeStr != null) {
            try {
                ThemeMode.valueOf(modeStr)
            } catch (e: Exception) {
                ThemeMode.SYSTEM_DEFAULT
            }
        } else {
            // Legacy fallback
            val useSystem = prefs[KEY_USE_SYSTEM_THEME] ?: true
            val isDark = prefs[KEY_DARK_THEME] ?: false
            when {
                useSystem -> ThemeMode.SYSTEM_DEFAULT
                isDark -> ThemeMode.DARK_AMOLED
                else -> ThemeMode.LIGHT
            }
        }
    }

    val useDynamicColor: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_DYNAMIC_COLOR] ?: true
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_THEME] ?: false
    }

    val useSystemTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_SYSTEM_THEME] ?: true
    }

    val isDevMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DEV_MODE] ?: true
    }

    val isSeedDataLoaded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SEED_DATA_LOADED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setLoggedIn(
        userId: String,
        userName: String,
        email: String,
        role: String = "STUDENT"
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = userName
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_ROLE] = role
        }
    }

    suspend fun setLoggedOut() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_ROLE)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
            when (mode) {
                ThemeMode.SYSTEM_DEFAULT -> {
                    prefs[KEY_USE_SYSTEM_THEME] = true
                    prefs[KEY_DARK_THEME] = false
                }
                ThemeMode.LIGHT -> {
                    prefs[KEY_USE_SYSTEM_THEME] = false
                    prefs[KEY_DARK_THEME] = false
                }
                ThemeMode.DARK_AMOLED -> {
                    prefs[KEY_USE_SYSTEM_THEME] = false
                    prefs[KEY_DARK_THEME] = true
                }
            }
        }
    }

    suspend fun setUseDynamicColor(useDynamic: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_USE_DYNAMIC_COLOR] = useDynamic
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = isDark
            prefs[KEY_THEME_MODE] = if (isDark) ThemeMode.DARK_AMOLED.name else ThemeMode.LIGHT.name
            prefs[KEY_USE_SYSTEM_THEME] = false
        }
    }

    suspend fun setUseSystemTheme(useSystem: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_USE_SYSTEM_THEME] = useSystem
            if (useSystem) {
                prefs[KEY_THEME_MODE] = ThemeMode.SYSTEM_DEFAULT.name
            }
        }
    }

    suspend fun setSeedDataLoaded(loaded: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_SEED_DATA_LOADED] = loaded
        }
    }

    suspend fun clearAllUserData() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_ROLE)
        }
    }
}
