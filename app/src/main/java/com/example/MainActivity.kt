package com.example

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavHost
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.ProgressViewModel
import com.example.util.LocalLanguage
import java.util.Locale

class MainActivity : ComponentActivity() {
  private lateinit var sharedPrefs: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    sharedPrefs = getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)

    // Initialize our MVVM ViewModel backed by Room Database
    val viewModel = ViewModelProvider(
      this, 
      TaskViewModel.provideFactory(application)
    )[TaskViewModel::class.java]

    val progressViewModel = ViewModelProvider(
      this,
      ProgressViewModel.provideFactory(application)
    )[ProgressViewModel::class.java]

    setContent {
      // 1. Language state
      var appLanguagePreference by remember {
        mutableStateOf(sharedPrefs.getString("app_language", "system") ?: "system")
      }

      // 2. Theme state
      var appThemePreference by remember {
        mutableStateOf(sharedPrefs.getString("app_theme", "system") ?: "system")
      }

      // 3. Font size state
      var appFontSizePreference by remember {
        mutableStateOf(sharedPrefs.getString("app_font_size", "normal") ?: "normal")
      }

      // Observe preference changes reactively
      DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
          if (key == "app_language") {
            appLanguagePreference = prefs.getString("app_language", "system") ?: "system"
          } else if (key == "app_theme") {
            appThemePreference = prefs.getString("app_theme", "system") ?: "system"
          } else if (key == "app_font_size") {
            appFontSizePreference = prefs.getString("app_font_size", "normal") ?: "normal"
          }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
          sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
      }

      // Resolve system language
      val resolvedLang = if (appLanguagePreference == "system") {
        val sysLang = Locale.getDefault().language
        if (sysLang == "fa") "fa" else "en"
      } else {
        appLanguagePreference
      }

      // Resolve theme mode
      val isDark = when (appThemePreference) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
      }

      // Resolve font size scale multiplier
      val resolvedScale = when (appFontSizePreference) {
        "small" -> 0.85f
        "large" -> 1.15f
        "xlarge" -> 1.30f
        else -> 1.0f
      }

      // Decide Layout Direction
      val layoutDirection = if (resolvedLang == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

      MyApplicationTheme(darkTheme = isDark, fontSizeScale = resolvedScale) {
        CompositionLocalProvider(
          LocalLayoutDirection provides layoutDirection,
          LocalLanguage provides resolvedLang
        ) {
          val navController = rememberNavController()
          AppNavHost(
            navController = navController,
            viewModel = viewModel,
            progressViewModel = progressViewModel,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}

