package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TaskViewModel
import com.example.util.LocalLanguage
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
    }

    val lang = LocalLanguage.current
    val trans = remember(lang) { { key: String -> Localization.getString(key, lang) } }

    // Persisted settings states
    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true))
    }
    var playReminderSound by remember {
        mutableStateOf(sharedPrefs.getBoolean("play_reminder_sound", true))
    }
    var defaultSnoozeTime by remember {
        mutableStateOf(sharedPrefs.getInt("default_snooze_time", 10))
    }
    var hideCompletedTasks by remember {
        mutableStateOf(sharedPrefs.getBoolean("hide_completed_tasks", false))
    }
    var appLangPref by remember {
        mutableStateOf(sharedPrefs.getString("app_language", "system") ?: "system")
    }
    var appThemePref by remember {
        mutableStateOf(sharedPrefs.getString("app_theme", "system") ?: "system")
    }
    var appFontSizePref by remember {
        mutableStateOf(sharedPrefs.getString("app_font_size", "normal") ?: "normal")
    }

    var snoozeDropdownExpanded by remember { mutableStateOf(false) }
    var modeDropdownExpanded by remember { mutableStateOf(false) }
    var showDisableNotificationsConfirmDialog by remember { mutableStateOf(false) }
    var showLanguageSelectionDialog by remember { mutableStateOf(false) }
    var showThemeSelectionDialog by remember { mutableStateOf(false) }
    var showFontSizeSelectionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = trans("settings_title"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = trans("back")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================== SECTION: LANGUAGE AND THEME ==================
            Text(
                text = trans("lang_settings_section"),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. App Language Select
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageSelectionDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("app_lang"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("app_lang_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = when (appLangPref) {
                                "fa" -> trans("farsi")
                                "en" -> trans("english")
                                else -> trans("system_default")
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. App Theme Select
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeSelectionDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("app_theme"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("app_theme_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = when (appThemePref) {
                                "dark" -> trans("dark_theme")
                                "light" -> trans("light_theme")
                                else -> trans("system_default")
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. App Font Size Select
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFontSizeSelectionDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("font_size_title"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("font_size_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = when (appFontSizePref) {
                                "small" -> trans("font_size_small")
                                "large" -> trans("font_size_large")
                                "xlarge" -> trans("font_size_xlarge")
                                else -> trans("font_size_normal")
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ================== SECTION: GENERAL SETTINGS ==================
            Text(
                text = trans("settings_general_title"),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Settings panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. Notification Enable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("notifications_active"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("notifications_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { isChecked ->
                                if (!isChecked && allTasks.any { it.isActive && !it.isCompleted }) {
                                    showDisableNotificationsConfirmDialog = true
                                } else {
                                    notificationsEnabled = isChecked
                                    sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                                }
                            },
                            modifier = Modifier.testTag("switch_notifications")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sound Toggle (New)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("play_reminder_sound_title"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("play_reminder_sound_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = playReminderSound,
                            onCheckedChange = { isChecked ->
                                playReminderSound = isChecked
                                sharedPrefs.edit().putBoolean("play_reminder_sound", isChecked).apply()
                            },
                            modifier = Modifier.testTag("switch_reminder_sound")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Default Snooze Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("default_snooze_title"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("default_snooze_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box {
                            Text(
                                text = trans("minutes_option").format(defaultSnoozeTime),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .clickable { snoozeDropdownExpanded = true }
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("snooze_selection")
                            )

                            DropdownMenu(
                                expanded = snoozeDropdownExpanded,
                                onDismissRequest = { snoozeDropdownExpanded = false }
                            ) {
                                listOf(5, 10, 15, 30).forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(trans("minutes_option").format(time)) },
                                        onClick = {
                                            defaultSnoozeTime = time
                                            sharedPrefs.edit().putInt("default_snooze_time", time).apply()
                                            snoozeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Completed view mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("hide_completed_title"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("hide_completed_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box {
                            Text(
                                text = if (hideCompletedTasks) trans("hide_tasks") else trans("show_normally"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .clickable { modeDropdownExpanded = true }
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("mode_selection")
                            )

                            DropdownMenu(
                                expanded = modeDropdownExpanded,
                                onDismissRequest = { modeDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(trans("show_normally")) },
                                    onClick = {
                                        hideCompletedTasks = false
                                        sharedPrefs.edit().putBoolean("hide_completed_tasks", false).apply()
                                        modeDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(trans("hide_tasks")) },
                                    onClick = {
                                        hideCompletedTasks = true
                                        sharedPrefs.edit().putBoolean("hide_completed_tasks", true).apply()
                                        modeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Default Calendar Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trans("calendar_type_title"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = trans("calendar_type_desc"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        var useGregorian by remember {
                            mutableStateOf(sharedPrefs.getBoolean("use_gregorian_calendar", false))
                        }
                        Switch(
                            checked = useGregorian,
                            onCheckedChange = { isChecked ->
                                useGregorian = isChecked
                                sharedPrefs.edit().putBoolean("use_gregorian_calendar", isChecked).apply()
                            },
                            modifier = Modifier.testTag("switch_use_gregorian")
                        )
                    }
                }
            }

            // ================== SECTION: SUGGESTED ROUTINES ==================
            Text(
                text = if (lang == "fa") "عادت‌ها و سبک زندگی" else "Habits & Lifestyle",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSuggestions() }
                    .testTag("action_settings_suggestions_row"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (lang == "fa") "روتین‌های پیشنهادی" else "Suggested Routines",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (lang == "fa") "بهبود سبک زندگی و سلامت فردی با عادات مفید" else "Improve lifestyle and custom daily wellness",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // simple back indicator as chevron
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ================== SECTION: ABOUT APPLICATION ==================
            Text(
                text = trans("about_app_section"),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trans("app_title"),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = trans("about_app_desc"),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = trans("package_id"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = context.packageName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = trans("app_version"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "v1.0.0",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // ================== DIALOGS ==================

    // 1. Language Selection Dialog
    if (showLanguageSelectionDialog) {
        val languages = listOf("system", "fa", "en")
        AlertDialog(
            onDismissRequest = { showLanguageSelectionDialog = false },
            title = { Text(text = trans("select_lang"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    languages.forEach { languageCode ->
                        val label = when (languageCode) {
                            "fa" -> trans("farsi")
                            "en" -> trans("english")
                            else -> trans("system_default")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (appLangPref == languageCode),
                                    onClick = {
                                        appLangPref = languageCode
                                        sharedPrefs.edit().putString("app_language", languageCode).apply()
                                        showLanguageSelectionDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (appLangPref == languageCode),
                                onClick = {
                                    appLangPref = languageCode
                                    sharedPrefs.edit().putString("app_language", languageCode).apply()
                                    showLanguageSelectionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageSelectionDialog = false }) {
                    Text(text = trans("cancel"))
                }
            }
        )
    }

    // 2. Theme Selection Dialog
    if (showThemeSelectionDialog) {
        val themes = listOf("system", "light", "dark")
        AlertDialog(
            onDismissRequest = { showThemeSelectionDialog = false },
            title = { Text(text = trans("select_theme"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { themeCode ->
                        val label = when (themeCode) {
                            "light" -> trans("light_theme")
                            "dark" -> trans("dark_theme")
                            else -> trans("system_default")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (appThemePref == themeCode),
                                    onClick = {
                                        appThemePref = themeCode
                                        sharedPrefs.edit().putString("app_theme", themeCode).apply()
                                        showThemeSelectionDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (appThemePref == themeCode),
                                onClick = {
                                    appThemePref = themeCode
                                    sharedPrefs.edit().putString("app_theme", themeCode).apply()
                                    showThemeSelectionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeSelectionDialog = false }) {
                    Text(text = trans("cancel"))
                }
            }
        )
    }

    // 3. Font Size Selection Dialog
    if (showFontSizeSelectionDialog) {
        val fontSizeOptions = listOf("small", "normal", "large", "xlarge")
        AlertDialog(
            onDismissRequest = { showFontSizeSelectionDialog = false },
            title = { Text(text = trans("select_font_size"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    fontSizeOptions.forEach { sizeCode ->
                        val label = when (sizeCode) {
                            "small" -> trans("font_size_small")
                            "large" -> trans("font_size_large")
                            "xlarge" -> trans("font_size_xlarge")
                            else -> trans("font_size_normal")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (appFontSizePref == sizeCode),
                                    onClick = {
                                        appFontSizePref = sizeCode
                                        sharedPrefs.edit().putString("app_font_size", sizeCode).apply()
                                        showFontSizeSelectionDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (appFontSizePref == sizeCode),
                                onClick = {
                                    appFontSizePref = sizeCode
                                    sharedPrefs.edit().putString("app_font_size", sizeCode).apply()
                                    showFontSizeSelectionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFontSizeSelectionDialog = false }) {
                    Text(text = trans("cancel"))
                }
            }
        )
    }

    // 4. Disable notifications alert
    if (showDisableNotificationsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisableNotificationsConfirmDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationsEnabled = false
                        sharedPrefs.edit().putBoolean("notifications_enabled", false).apply()
                        showDisableNotificationsConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_disable_notifications_button")
                ) {
                    Text(text = trans("ok"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDisableNotificationsConfirmDialog = false }
                ) {
                    Text(text = trans("cancel"))
                }
            },
            title = {
                Text(text = trans("disable_notif_dialog_title"), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = trans("disable_notif_dialog_text"))
            }
        )
    }
}
