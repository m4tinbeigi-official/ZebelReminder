package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurrenceType
import com.example.data.model.RoutineSuggestion
import com.example.data.model.Task
import com.example.ui.viewmodel.TaskViewModel
import com.example.util.DateUtils
import com.example.util.JalaliCalendarHelper
import com.example.util.LocalLanguage
import com.example.util.Localization
import com.example.util.RoutineSuggestionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedRoutinesScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = LocalLanguage.current
    val trans = remember(lang) { { key: String -> Localization.getString(key, lang) } }

    val allTasks by viewModel.allTasks.collectAsState()

    // Identify which routines are already added to user's tasks
    val addedRoutineIds = remember(allTasks) {
        allTasks.filter { it.isRoutine && it.isActive && !it.isCompleted }
            .mapNotNull { it.routineSuggestionId }
            .toSet()
    }

    // List of routine suggestions
    val suggestions = remember { RoutineSuggestionProvider.suggestions }

    // State for Filter selections
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedFrequency by remember { mutableStateOf<String?>(null) } // "all", "daily", "weekly", "monthly"

    // Dialog state for setting customized reminder time
    var activeTimePickerDialogRoutine by remember { mutableStateOf<RoutineSuggestion?>(null) }
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    // Categories list exactly as requested
    val categories = remember {
        listOf(
            "سلامت و انرژی",
            "تمرکز و بهره‌وری",
            "رشد فردی",
            "نظم شخصی",
            "آرامش ذهن",
            "روابط و خانواده",
            "مدیریت مالی",
            "خانه و زندگی"
        )
    }

    // Frequency options
    val frequencies = remember {
        listOf(
            "all" to if (lang == "fa") "همه بازه‌ها" else "All Intervals",
            "daily" to if (lang == "fa") "روزانه" else "Daily",
            "weekly" to if (lang == "fa") "هفتگی" else "Weekly",
            "monthly" to if (lang == "fa") "ماهیانه" else "Monthly"
        )
    }

    // Filtered lists
    val filteredSuggestions = remember(selectedCategory, selectedFrequency) {
        suggestions.filter { suggestion ->
            val matchCat = selectedCategory == null || suggestion.category == selectedCategory
            val matchFreq = selectedFrequency == null || selectedFrequency == "all" || suggestion.frequency == selectedFrequency
            matchCat && matchFreq
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "fa") "روتین‌های پیشنهادی" else "Suggested Routines",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("suggestions_back_button")
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
        ) {
            // Static Safety / Disclaimer Callout
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "هشدار",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (lang == "fa") {
                            "این پیشنهادها عمومی هستند و جایگزین مشاوره تخصصی پزشکی، روانشناسی یا مالی نیستند."
                        } else {
                            "These suggestions are for educational purposes and do not substitute medical, psychological or financial advice."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Horizontal Category Filters
            Text(
                text = if (lang == "fa") "دسته‌بندی موضوعی:" else "Categories:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(if (lang == "fa") "همه موضوعات" else "All Categories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Frequency Filters
            Text(
                text = if (lang == "fa") "بازه انجام روتین:" else "Frequency:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(frequencies) { (key, label) ->
                    val isSelected = (selectedFrequency == key) || (selectedFrequency == null && key == "all")
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFrequency = if (key == "all") null else key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Items List
            if (filteredSuggestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "fa") "رکوردی با مشخصات انتخاب شده پیدا نشد." else "No suggested routines matched this criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSuggestions, key = { it.id }) { suggestion ->
                        val isAdded = addedRoutineIds.contains(suggestion.id)
                        RoutineSuggestionCard(
                            suggestion = suggestion,
                            isAdded = isAdded,
                            lang = lang,
                            onAddClick = {
                                // Extract hour/minute from default or set template values
                                val timeParts = (suggestion.suggestedTime ?: "08:00").split(":")
                                selectedHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                                selectedMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                                activeTimePickerDialogRoutine = suggestion
                            },
                            onManageClick = {
                                // Find associated active task to open detail screen
                                val record = allTasks.find { it.isRoutine && it.routineSuggestionId == suggestion.id && it.isActive && !it.isCompleted }
                                if (record != null) {
                                    onNavigateToTaskDetail(record.id)
                                } else {
                                    Toast.makeText(context, if (lang == "fa") "یادآور روتین یافت نشد" else "Routine task not found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Time picker alert dialog before adding suggested routines!
    val currentPickingRoutine = activeTimePickerDialogRoutine
    if (currentPickingRoutine != null) {
        AlertDialog(
            onDismissRequest = { activeTimePickerDialogRoutine = null },
            title = {
                Text(
                    text = if (lang == "fa") "انتخاب ساعت یادآوری" else "Select Reminder Time",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "fa") {
                            "روتین شما در ساعت زیر بصورت تکرارشونده زنگ خواهد خورد:"
                        } else {
                            "This routine will trigger periodically at the following time:"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Selector Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.width(64.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                Button(
                                    onClick = { selectedHour = (selectedHour + 1) % 24 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val displayedHour = if (lang == "fa") JalaliCalendarHelper.englishToPersianDigits(String.format("%02d", selectedHour)) else String.format("%02d", selectedHour)
                                Text(displayedHour, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { selectedHour = (selectedHour + 23) % 24 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))

                        // Minute Selector Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.width(64.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                Button(
                                    onClick = { selectedMinute = (selectedMinute + 5) % 60 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val displayedMinute = if (lang == "fa") JalaliCalendarHelper.englishToPersianDigits(String.format("%02d", selectedMinute)) else String.format("%02d", selectedMinute)
                                Text(displayedMinute, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { selectedMinute = (selectedMinute + 55) % 60 },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalHourStr = String.format("%02d", selectedHour)
                        val finalMinuteStr = String.format("%02d", selectedMinute)
                        val triggerTime = "$finalHourStr:$finalMinuteStr"

                        // Convert suggested routine into TaskEntity
                        val mappedRecurrence = when (currentPickingRoutine.frequency) {
                            "daily" -> RecurrenceType.DAILY
                            "weekly" -> RecurrenceType.WEEKLY
                            "monthly" -> RecurrenceType.MONTHLY
                            else -> RecurrenceType.DAILY
                        }

                        // Prepare descriptions with motivational quote
                        val summaryDesc = "${currentPickingRoutine.shortDescription}\n\n💡 پیشنهاد انگیزشی: ${currentPickingRoutine.motivationalText}"

                        viewModel.addTask(
                            title = currentPickingRoutine.title,
                            description = summaryDesc,
                            category = currentPickingRoutine.category,
                            recurrenceType = mappedRecurrence,
                            dueDate = DateUtils.getTodayDateString(),
                            dueTime = triggerTime,
                            selectedWeekdays = null,
                            customIntervalDays = null,
                            snoozeMinutes = currentPickingRoutine.defaultSnoozeMinutes,
                            isRoutine = true,
                            routineCategory = currentPickingRoutine.category,
                            routineGoalType = "binary",
                            routineTargetCount = 1,
                            routinePeriod = currentPickingRoutine.frequency,
                            routineSuggestionId = currentPickingRoutine.id,
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    if (lang == "fa") "روتین «${currentPickingRoutine.title}» با موفقیت اضافه شد!" else "Routine '${currentPickingRoutine.title}' integrated successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onFailure = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Also find the newly inserted task and update its isRoutine/routineSuggestionId details if needed or handled in ViewModel.
                        // Wait! To be extremely precise, we can update TaskViewModel.kt or handle it smoothly by making a tiny edit/logic,
                        // or we can just append properties inside the ViewModel's addTask function!
                        // Let's make sure addTask handles isRoutine. We'll edit `TaskViewModel.kt`'s `addTask` to accept optional routine arguments!
                        // That is extremely robust and avoids missing fields!

                        activeTimePickerDialogRoutine = null
                    }
                ) {
                    Text(if (lang == "fa") "افزودن به برنامه" else "Add Habit")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeTimePickerDialogRoutine = null }) {
                    Text(trans("cancel"))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
fun RoutineSuggestionCard(
    suggestion: RoutineSuggestion,
    isAdded: Boolean,
    lang: String,
    onAddClick: () -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdded) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Badge & Frequency Label Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = suggestion.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Frequency Label Display
                val localizedFreq = when (suggestion.frequency) {
                    "daily" -> if (lang == "fa") "هر روز" else "Daily"
                    "weekly" -> if (lang == "fa") "هر هفته" else "Weekly"
                    "monthly" -> if (lang == "fa") "هر ماه" else "Monthly"
                    else -> suggestion.frequency
                }
                Text(
                    text = localizedFreq,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Body Description
            Text(
                text = suggestion.shortDescription,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Motivation Quotes Callout Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "انگیزشی",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suggestion.motivationalText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == "fa") "اضافه شده" else "Added",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Button(
                            onClick = onManageClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == "fa") "مدیریت روتین" else "Manage Habit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "fa") "افزودن به روتین‌های من" else "Add to My Routines",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
