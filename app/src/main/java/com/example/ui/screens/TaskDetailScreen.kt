package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.util.DateUtils
import com.example.util.LocalLanguage
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskViewModel,
    taskId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val task = allTasks.find { it.id == taskId }
    val context = LocalContext.current
    val lang = LocalLanguage.current
    val trans = { key: String -> Localization.getString(key, lang) }

    val sharedPrefs = remember {
        context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
    }
    val useGregorian = sharedPrefs.getBoolean("use_gregorian_calendar", false)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRecurringCompleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = trans("task_details"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = trans("back")
                        )
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(
                            onClick = { onNavigateToEdit(task.id) },
                            modifier = Modifier.testTag("detail_edit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = trans("edit"))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (task == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (lang == "fa") "یادآور مورد نظر یافت نشد." else "The requested reminder was not found.",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.error)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (task.isCompleted) {
                                if (lang == "fa") "کار با موفقیت انجام شده است" else "Task completed successfully"
                            } else {
                                if (lang == "fa") "یادآور فعال است" else "Reminder is active"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Description Box
                if (task.description.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (lang == "fa") "توضیحات یادآور" else "Task Description",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Attributes List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val friendlyLabel = DateUtils.getFriendlyDateLabel(task.dueDate)
                        val friendlyDate = if (useGregorian) {
                            friendlyLabel
                        } else {
                            if (friendlyLabel == "امروز") trans("today")
                            else if (friendlyLabel == "فردا") if (lang == "fa") "فردا" else "Tomorrow"
                            else com.example.util.JalaliCalendarHelper.gregorianStrToJalaliStr(task.dueDate)
                        }
                        val formattedDate = if (useGregorian || lang == "en") friendlyDate else com.example.util.JalaliCalendarHelper.englishToPersianDigits(friendlyDate)
                        val formattedTime = if (useGregorian || lang == "en") task.dueTime else com.example.util.JalaliCalendarHelper.englishToPersianDigits(task.dueTime)

                        DetailItem(
                            icon = Icons.Default.CalendarMonth,
                            title = trans("planned_date"),
                            value = formattedDate
                        )
                        HorizontalDivider()
                        DetailItem(
                            icon = Icons.Default.Schedule,
                            title = trans("reminder_time"),
                            value = formattedTime
                        )
                        HorizontalDivider()

                        val localizedRecurrenceTitle = when (task.recurrenceType) {
                            com.example.data.model.RecurrenceType.ONE_TIME -> trans("recurrence_none")
                            com.example.data.model.RecurrenceType.DAILY -> trans("recurrence_daily")
                            com.example.data.model.RecurrenceType.WEEKLY -> trans("recurrence_weekly")
                            com.example.data.model.RecurrenceType.MONTHLY -> trans("recurrence_monthly")
                            com.example.data.model.RecurrenceType.YEARLY -> if (lang == "fa") "هر سال" else "Yearly"
                            com.example.data.model.RecurrenceType.EVERY_X_DAYS -> trans("recurrence_custom")
                            com.example.data.model.RecurrenceType.CUSTOM_WEEKDAYS -> trans("recurrence_weekdays")
                        }
                        DetailItem(
                            icon = Icons.Default.Loop,
                            title = trans("recurrence_type"),
                            value = localizedRecurrenceTitle
                        )

                        if (!task.category.isNullOrBlank()) {
                            HorizontalDivider()
                            DetailItem(
                                icon = Icons.Default.Category,
                                title = if (lang == "fa") "دسته‌بندی" else "Category",
                                value = task.category
                            )
                        }

                        HorizontalDivider()
                        DetailItem(
                            icon = Icons.Default.Snooze,
                            title = trans("snooze_duration"),
                            value = trans("minutes_option").format(task.snoozeMinutes)
                        )
                    }
                }

                // Quick Status & Snooze buttons
                Button(
                    onClick = {
                        if (!task.isCompleted && task.recurrenceType != com.example.data.model.RecurrenceType.ONE_TIME) {
                            showRecurringCompleteConfirmDialog = true
                        } else {
                            viewModel.toggleTaskCompletion(task) { msg ->
                                val localizedMsg = if (msg.contains("موفقیت")) {
                                    if (task.isCompleted) trans("toast_task_status_restored") else trans("task_status_updated")
                                } else msg
                                android.widget.Toast.makeText(context, localizedMsg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("detail_toggle_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        text = if (task.isCompleted) {
                            if (lang == "fa") "بازنشانی به فعال" else "Restore to Active"
                        } else {
                            if (lang == "fa") "علامت انجام شد" else "Mark Completed"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!task.isCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (lang == "fa") "گزینه‌های تعویق اعلان" else "Snooze Alarm Options",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 5 Minutes
                        OutlinedButton(
                            onClick = {
                                viewModel.snoozeTask(task, 5) { msg ->
                                    val locMsg = if (msg.contains("خوابانده")) {
                                        if (lang == "fa") "اعلان برای ۵ دقیقه بعد به تعویق افتاد." else "Alarm was snoozed for 5 minutes."
                                    } else msg
                                    android.widget.Toast.makeText(context, locMsg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("snooze_5_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (lang == "fa") "۵ دقیقه" else "5 Mins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 10 Minutes
                        OutlinedButton(
                            onClick = {
                                viewModel.snoozeTask(task, 10) { msg ->
                                    val locMsg = if (msg.contains("خوابانده")) {
                                        if (lang == "fa") "اعلان برای ۱۰ دقیقه بعد به تعویق افتاد." else "Alarm was snoozed for 10 minutes."
                                    } else msg
                                    android.widget.Toast.makeText(context, locMsg, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("snooze_10_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (lang == "fa") "۱۰ دقیقه" else "10 Mins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Custom Snooze
                        var showCustomSnoozeDialog by remember { mutableStateOf(false) }
                        var customMinutesStr by remember { mutableStateOf("15") }

                        OutlinedButton(
                            onClick = { showCustomSnoozeDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("snooze_custom_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (lang == "fa") "تعویق سفارشی" else "Custom Snooze", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (showCustomSnoozeDialog) {
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showCustomSnoozeDialog = false },
                                title = {
                                    Text(
                                        text = if (lang == "fa") "به تعویق انداختن سفارشی" else "Custom Snooze Limit",
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                text = {
                                    Column {
                                        Text(text = if (lang == "fa") "مدت زمان تعویق را به دقیقه وارد کنید:" else "Enter snooze duration in minutes:")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = customMinutesStr,
                                            onValueChange = { input ->
                                                if (input.all { it.isDigit() }) {
                                                    customMinutesStr = input
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth().testTag("custom_snooze_input"),
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            ),
                                            singleLine = true
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val mins = customMinutesStr.toIntOrNull() ?: 15
                                            viewModel.snoozeTask(task, mins) { msg ->
                                                val locMsg = if (msg.contains("خوابانده")) {
                                                    if (lang == "fa") "اعلان برای $mins دقیقه بعد به تعویق افتاد." else "Alarm was snoozed for $mins minutes."
                                                } else msg
                                                android.widget.Toast.makeText(context, locMsg, android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            showCustomSnoozeDialog = false
                                        },
                                        modifier = Modifier.testTag("confirm_custom_snooze")
                                    ) {
                                        Text(trans("ok"), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCustomSnoozeDialog = false }) {
                                        Text(trans("cancel"))
                                    }
                                }
                            )
                        }
                    }
                }

                // Delete Action
                Button(
                    onClick = {
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("detail_delete_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "fa") "حذف این یادآور" else "Delete Reminder",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showDeleteDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(trans("delete_task_title"), fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(trans("delete_task_msg"))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteTask(task.id, onSuccess = {
                                        android.widget.Toast.makeText(context, trans("toast_task_deleted_success"), android.widget.Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }, onFailure = { errorMsg ->
                                        android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    })
                                },
                                modifier = Modifier.testTag("confirm_delete_button")
                            ) {
                                Text(trans("yes_delete_confirm"), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteDialog = false },
                                modifier = Modifier.testTag("dismiss_delete_button")
                            ) {
                                Text(trans("cancel"))
                            }
                        }
                    )
                }

                if (showRecurringCompleteConfirmDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showRecurringCompleteConfirmDialog = false },
                        title = {
                            Text(trans("complete_recurring_reminder_dialog_title"), fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(trans("complete_recurring_reminder_dialog_desc"))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRecurringCompleteConfirmDialog = false
                                    viewModel.toggleTaskCompletion(task) { msg ->
                                        val localizedMsg = if (msg.contains("موفقیت")) trans("task_status_updated") else msg
                                        android.widget.Toast.makeText(context, localizedMsg, android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.testTag("confirm_recurring_complete_button")
                            ) {
                                Text(trans("yes_completed_confirm"), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showRecurringCompleteConfirmDialog = false },
                                modifier = Modifier.testTag("dismiss_recurring_complete_button")
                            ) {
                                Text(trans("cancel"))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
