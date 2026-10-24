package com.rekora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rekora.app.data.datastore.RekoraPreferences
import com.rekora.app.data.datastore.ThemeMode
import com.rekora.app.ui.navigation.RekoraNavHost
import com.rekora.app.ui.theme.RekoraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: RekoraPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by preferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM_DEFAULT)
            val useDynamicColor by preferences.useDynamicColor.collectAsState(initial = true)

            RekoraTheme(
                themeMode = themeMode,
                dynamicColor = useDynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RekoraNavHost()
                }
            }
        }
    }
}
