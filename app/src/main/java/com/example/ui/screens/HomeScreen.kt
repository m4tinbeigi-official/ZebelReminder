package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Task
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.ProgressViewModel
import com.example.util.DateUtils
import com.example.util.LocalLanguage
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onNavigateToAddEdit: (taskId: Int?) -> Unit,
    onNavigateToDetail: (taskId: Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSuggestions: () -> Unit,
    modifier: Modifier = Modifier,
    progressViewModel: ProgressViewModel? = null
) {
    val todayTasks by viewModel.todayTasks.collectAsState()
    val upcomingTasks by viewModel.upcomingTasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    val context = LocalContext.current
    val lang = LocalLanguage.current
    val trans = remember(lang) { { key: String -> Localization.getString(key, lang) } }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.widget.Toast.makeText(context, trans("toast_reminder_scheduled"), android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, trans("toast_must_grant_notifications"), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                showPermissionDialog = true
            }
        }
    }

    val sharedPrefs = remember {
        context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
    }
    // Read local mode
    val hideCompleted = sharedPrefs.getBoolean("hide_completed_tasks", false)
    val useGregorian = sharedPrefs.getBoolean("use_gregorian_calendar", false)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = remember(lang) { listOf(trans("today"), trans("future"), trans("overdue"), trans("completed")) }
    var showDeleteDialogForTaskId by remember { mutableStateOf<Int?>(null) }
    var showRecurringCompleteConfirmTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trans("app_title"),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.testTag("action_about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = trans("about_dev"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onNavigateToProgress,
                        modifier = Modifier.testTag("action_progress_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = if (lang == "fa") "برنامه سلامت و خودمراقبتی" else "Self-care Routines",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("action_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = trans("settings_title"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddEdit(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(trans("add_new_reminder_btn"), fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_task_fab")
                    .padding(8.dp)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Task count dashboard cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val totalRemaining = todayTasks.size + overdueTasks.size + upcomingTasks.size
                DashboardCountCard(
                    title = trans("active_tasks_card"),
                    count = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(totalRemaining.toString()) else totalRemaining.toString(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                DashboardCountCard(
                    title = trans("today_card"),
                    count = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(todayTasks.size.toString()) else todayTasks.size.toString(),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                DashboardCountCard(
                    title = trans("completed_card"),
                    count = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(completedTasks.size.toString()) else completedTasks.size.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Lifestyle progress mini-card
            val progressUiState = progressViewModel?.uiState?.collectAsState()?.value
            val pct = progressUiState?.todayPercentage ?: 0
            val strk = progressUiState?.currentDailyStreak ?: 0
            val pctFriendly = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(pct.toString()) else pct.toString()
            val strkFriendly = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(strk.toString()) else strk.toString()
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToProgress() }
                    .testTag("lifestyle_progress_mini_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == "fa") "پیشرفت سبک زندگی" else "Lifestyle Progress",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "fa") {
                                    "امروز $pctFriendly٪ از روتین‌هایت را انجام داده‌ای. زنجیره فعلی: $strkFriendly روز."
                                } else {
                                    "Today you completed $pctFriendly% of your routines. Current streak: $strkFriendly days."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = onNavigateToProgress,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (lang == "fa") "مشاهده گزارش" else "View Report",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Suggested routine prompt when user has zero active routines
            val hasActiveRoutines = allTasks.any { it.isRoutine && it.isActive }
            if (!hasActiveRoutines) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("suggested_routine_prompt_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (lang == "fa") "برای بهبود سبک زندگی، چند روتین ساده کاربری ایجاد کن." else "To improve your lifestyle, add a few simple self-care routines.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Button(
                            onClick = onNavigateToSuggestions,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (lang == "fa") "مشاهده روتین‌های پیشنهادی" else "View Suggested Routines",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Beautiful Suggested Routines Hero Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToSuggestions() }
                    .testTag("action_home_suggestions_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == "fa") "روتین‌های پیشنهادی خودمراقبتی" else "Suggested Lifestyle Routines",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (lang == "fa") "عادت‌های روزانه، هفتگی و ماهانه را به برنامه خود اضافه کنید" else "Add recommended water/study/sports to your reminders list",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Tab navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Tab navigation task list
            val rawTasks = when (selectedTabIndex) {
                0 -> todayTasks
                1 -> upcomingTasks
                2 -> overdueTasks
                else -> completedTasks
            }

            // If local setting says hide completed tasks, we filter completed out except in completed tab itself!
            val currentTasks = if (hideCompleted && selectedTabIndex != 3) {
                rawTasks.filter { !it.isCompleted }
            } else {
                rawTasks
            }

            val todayDateStr = remember { DateUtils.getTodayDateString() }
            val (todayRoutines, todayNormals) = remember(currentTasks, selectedTabIndex) {
                if (selectedTabIndex == 0) {
                    val routines: List<Task> = currentTasks.filter { it.isRoutine && it.isActive && it.dueDate == todayDateStr }
                    val normals: List<Task> = currentTasks.filter { !it.isRoutine || !it.isActive || it.dueDate != todayDateStr }
                    Pair(routines, normals)
                } else {
                    Pair(emptyList<Task>(), currentTasks)
                }
            }

            if (allTasks.isEmpty()) {
                // Global Empty State
                EmptyStateView(
                    message = trans("no_tasks_added_yet"),
                    description = trans("tap_plus_to_add_first")
                )
            } else if (currentTasks.isEmpty()) {
                // Tab-specific empty states
                EmptyStateView(
                    message = when (selectedTabIndex) {
                        0 -> trans("no_tasks_for_today")
                        1 -> trans("no_tasks_for_future")
                        2 -> trans("no_tasks_overdue")
                        else -> trans("no_tasks_completed")
                    },
                    description = when (selectedTabIndex) {
                        0 -> trans("can_start_day_add_task")
                        else -> null
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTabIndex == 0 && todayRoutines.isNotEmpty()) {
                        item {
                            Text(
                                text = if (lang == "fa") "روتین‌های امروز" else "Today's Routines",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(todayRoutines, key = { "routine_${it.id}" }) { task ->
                            TodayRoutineItem(
                                task = task,
                                onCompleteClick = {
                                    if (task.recurrenceType != com.example.data.model.RecurrenceType.ONE_TIME) {
                                        showRecurringCompleteConfirmTask = task
                                    } else {
                                        viewModel.toggleTaskCompletion(task) { msg ->
                                            android.widget.Toast.makeText(context, trans("task_status_updated"), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onSnoozeClick = { min ->
                                    viewModel.snoozeTask(task, min) {
                                        android.widget.Toast.makeText(context, if (lang == "fa") "یادآوری به تعویق افتاد." else "Reminder snoozed.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDetailClick = { onNavigateToDetail(task.id) },
                                lang = lang
                            )
                        }

                        if (todayNormals.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (lang == "fa") "تسک‌های امروز" else "Today's Tasks",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    items(todayNormals, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onCompleteToggle = {
                                if (!task.isCompleted && task.recurrenceType != com.example.data.model.RecurrenceType.ONE_TIME) {
                                    showRecurringCompleteConfirmTask = task
                                } else {
                                    viewModel.toggleTaskCompletion(task) { msg ->
                                        val localizedMsg = if (msg.contains("موفقیت")) {
                                            if (task.isCompleted) trans("toast_task_status_restored") else trans("task_status_updated")
                                        } else msg
                                        android.widget.Toast.makeText(context, localizedMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onEditClick = { onNavigateToAddEdit(task.id) },
                            onDeleteClick = { showDeleteDialogForTaskId = task.id },
                            onTaskClick = { onNavigateToDetail(task.id) },
                            useGregorian = useGregorian,
                            lang = lang
                        )
                    }
                }
            }

            if (showDeleteDialogForTaskId != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteDialogForTaskId = null },
                    title = {
                        Text(
                            text = trans("delete_task_title"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = trans("delete_task_msg"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val taskId = showDeleteDialogForTaskId
                                if (taskId != null) {
                                    viewModel.deleteTask(taskId, onSuccess = {
                                        android.widget.Toast.makeText(context, trans("toast_task_deleted_success"), android.widget.Toast.LENGTH_SHORT).show()
                                    }, onFailure = { errorMsg ->
                                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    })
                                }
                                showDeleteDialogForTaskId = null
                            },
                            modifier = Modifier.testTag("confirm_delete_button")
                        ) {
                            Text(trans("yes_delete_confirm"), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialogForTaskId = null },
                            modifier = Modifier.testTag("dismiss_delete_button")
                        ) {
                            Text(trans("cancel"), fontWeight = FontWeight.Medium)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            val currentConfirmTask = showRecurringCompleteConfirmTask
            if (currentConfirmTask != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showRecurringCompleteConfirmTask = null },
                    title = {
                        Text(
                            text = trans("complete_recurring_reminder_dialog_title"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = trans("complete_recurring_reminder_dialog_desc"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.toggleTaskCompletion(currentConfirmTask) { msg ->
                                    val localizedMsg = if (msg.contains("موفقیت")) trans("task_status_updated") else msg
                                    android.widget.Toast.makeText(context, localizedMsg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                showRecurringCompleteConfirmTask = null
                            },
                            modifier = Modifier.testTag("confirm_recurring_complete_button")
                        ) {
                            Text(trans("yes_completed_confirm"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showRecurringCompleteConfirmTask = null },
                            modifier = Modifier.testTag("dismiss_recurring_complete_button")
                        ) {
                            Text(trans("cancel"), fontWeight = FontWeight.Medium)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            if (showPermissionDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = trans("enable_notifications_permission"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = trans("notifications_permission_requirement_desc"),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified // comfortable height
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        ) {
                            Text(trans("confirm_and_enable"), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                android.widget.Toast.makeText(context, trans("toast_must_grant_notifications"), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(trans("later"))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}

@Composable
fun DashboardCountCard(
    title: String,
    count: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: Task,
    onCompleteToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTaskClick: () -> Unit,
    useGregorian: Boolean = false,
    lang: String = "fa",
    modifier: Modifier = Modifier
) {
    val trans = remember(lang) { { key: String -> Localization.getString(key, lang) } }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCompleteToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("task_check_${task.id}")
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = trans("complete"),
                            tint = if (task.isCompleted) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (task.isCompleted) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (task.isRoutine) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (lang == "fa") "روتین" else "Routine",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Show Category tag if present
                if (!task.category.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Due date, time, and recurrence display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                val friendlyLabel = DateUtils.getFriendlyDateLabel(task.dueDate)
                val friendlyDate = if (useGregorian) {
                    friendlyLabel
                } else {
                    if (friendlyLabel == "امروز") trans("today")
                    else if (friendlyLabel == "فردا") if (lang == "fa") "فردا" else "Tomorrow"
                    else com.example.util.JalaliCalendarHelper.gregorianStrToJalaliStr(task.dueDate)
                }

                val rawTimeAndDateStr = "${task.dueTime} - $friendlyDate"
                val displayTimeAndDate = if (useGregorian || lang == "en") rawTimeAndDateStr else com.example.util.JalaliCalendarHelper.englishToPersianDigits(rawTimeAndDateStr)

                Text(
                    text = displayTimeAndDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Map recurrence type titles to localized strings
                val localizedRecurrenceTitle = when (task.recurrenceType) {
                    com.example.data.model.RecurrenceType.ONE_TIME -> trans("recurrence_none")
                    com.example.data.model.RecurrenceType.DAILY -> trans("recurrence_daily")
                    com.example.data.model.RecurrenceType.WEEKLY -> trans("recurrence_weekly")
                    com.example.data.model.RecurrenceType.MONTHLY -> trans("recurrence_monthly")
                    com.example.data.model.RecurrenceType.YEARLY -> if (lang == "fa") "هر سال" else "Yearly"
                    com.example.data.model.RecurrenceType.EVERY_X_DAYS -> trans("recurrence_custom")
                    com.example.data.model.RecurrenceType.CUSTOM_WEEKDAYS -> trans("recurrence_weekdays")
                }

                Text(
                    text = "• $localizedRecurrenceTitle",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(4.dp))

            // Action row mapping: انجام شد / جزئیات / ویرایش / حذف
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group left actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. انجام شد (toggle)
                    TextButton(
                        onClick = onCompleteToggle,
                        modifier = Modifier.testTag("action_toggle_${task.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.Refresh else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (task.isCompleted) trans("reset_task_or_refresh") else trans("done"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // 2. جزئیات
                    TextButton(
                        onClick = { onTaskClick() },
                        modifier = Modifier.testTag("action_details_${task.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trans("details"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // 3. ویرایش
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("action_edit_${task.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trans("edit"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 4. حذف (Rightmost action)
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("action_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = trans("delete"),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Event,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TodayRoutineItem(
    task: Task,
    onCompleteClick: () -> Unit,
    onSnoozeClick: (Int) -> Unit,
    onDetailClick: () -> Unit,
    lang: String = "fa"
) {
    var showSnoozeMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_routine_item_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!task.category.isNullOrBlank()) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (lang == "fa") com.example.util.JalaliCalendarHelper.englishToPersianDigits(task.dueTime) else task.dueTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val recLabel = when (task.recurrenceType) {
                            com.example.data.model.RecurrenceType.DAILY -> if (lang == "fa") "روزانه" else "Daily"
                            com.example.data.model.RecurrenceType.WEEKLY -> if (lang == "fa") "هفتگی" else "Weekly"
                            com.example.data.model.RecurrenceType.MONTHLY -> if (lang == "fa") "ماهانه" else "Monthly"
                            else -> if (lang == "fa") "عادی" else "Once"
                        }
                        Text(
                            text = recLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (lang == "fa") "روتین" else "Routine",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCompleteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == "fa") "انجام شد" else "Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box {
                    OutlinedButton(
                        onClick = { showSnoozeMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == "fa") "تعویق" else "Snooze",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSnoozeMenu,
                        onDismissRequest = { showSnoozeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (lang == "fa") "۵ دقیقه" else "5 Minutes") },
                            onClick = {
                                onSnoozeClick(5)
                                showSnoozeMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (lang == "fa") "۱۰ دقیقه" else "10 Minutes") },
                            onClick = {
                                onSnoozeClick(10)
                                showSnoozeMenu = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onDetailClick,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (lang == "fa") "جزئیات" else "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
