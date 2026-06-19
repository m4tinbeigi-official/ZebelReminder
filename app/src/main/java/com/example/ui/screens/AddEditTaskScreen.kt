package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurrenceType
import com.example.data.model.Task
import com.example.ui.components.CustomDatePickerDialog
import com.example.ui.components.CustomTimePickerDialog
import com.example.ui.viewmodel.TaskViewModel
import com.example.util.DateUtils
import com.example.util.JalaliCalendarHelper
import com.example.util.LocalLanguage
import com.example.util.Localization
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val lang = LocalLanguage.current
    val trans = { key: String -> Localization.getString(key, lang) }

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.ONE_TIME) }
    var dueDate by remember { mutableStateOf(DateUtils.getTodayDateString()) }
    var dueTime by remember { mutableStateOf(DateUtils.getCurrentTimeString()) }
    
    // Snooze options: "۵ دقیقه", "۱۰ دقیقه", "سفارشی"
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("zebel_settings", android.content.Context.MODE_PRIVATE)
    }
    val defaultSnoozeVal = remember { sharedPrefs.getInt("default_snooze_time", 10) }
    
    val initialSnoozeOption = remember {
        when (defaultSnoozeVal) {
            5 -> if (lang == "fa") "۵ دقیقه" else "5 Mins"
            10 -> if (lang == "fa") "۱۰ دقیقه" else "10 Mins"
            else -> if (lang == "fa") "سفارشی" else "Custom"
        }
    }
    var snoozeOption by remember { mutableStateOf(initialSnoozeOption) }
    
    // Auto-update snoozeOption based on dynamic language selection changes
    LaunchedEffect(lang) {
        snoozeOption = when (defaultSnoozeVal) {
            5 -> if (lang == "fa") "۵ دقیقه" else "5 Mins"
            10 -> if (lang == "fa") "۱۰ دقیقه" else "10 Mins"
            else -> if (lang == "fa") "سفارشی" else "Custom"
        }
    }

    var customSnoozeMinutes by remember {
        mutableStateOf(if (defaultSnoozeVal != 5 && defaultSnoozeVal != 10) defaultSnoozeVal.toString() else "15")
    }

    // Weekdays selector set for CUSTOM_WEEKDAYS
    // Indices: 0=شنبه, 1=یکشنبه, 2=دوشنبه, 3=سه‌شنبه, 4=چهارشنبه, 5=پنجشنبه, 6=جمعه
    var selectedWeekdaysSet by remember { mutableStateOf(emptySet<Int>()) }
    
    // Interval selector for EVERY_X_DAYS
    var customIntervalDays by remember { mutableStateOf("3") }

    var isEditMode by remember { mutableStateOf(false) }
    
    val useGregorian = remember { sharedPrefs.getBoolean("use_gregorian_calendar", false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var originalTask: Task? by remember { mutableStateOf(null) }

    // Inline errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var customIntervalError by remember { mutableStateOf<String?>(null) }
    var weekdaysError by remember { mutableStateOf<String?>(null) }

    // Prepopulate if in edit mode
    LaunchedEffect(taskId, allTasks) {
        if (taskId != null) {
            val task = allTasks.find { it.id == taskId }
            if (task != null) {
                isEditMode = true
                originalTask = task
                title = task.title
                description = task.description
                category = task.category ?: ""
                recurrenceType = task.recurrenceType
                dueDate = task.dueDate
                dueTime = task.dueTime
                
                // Set snooze option
                if (task.snoozeMinutes == 5) {
                    snoozeOption = if (lang == "fa") "۵ دقیقه" else "5 Mins"
                } else if (task.snoozeMinutes == 10) {
                    snoozeOption = if (lang == "fa") "۱۰ دقیقه" else "10 Mins"
                } else {
                    snoozeOption = if (lang == "fa") "سفارشی" else "Custom"
                    customSnoozeMinutes = task.snoozeMinutes.toString()
                }

                // Weekdays set
                val parsedSet = task.selectedWeekdays?.split(",")
                    ?.mapNotNull { it.toIntOrNull() }
                    ?.toSet() ?: emptySet()
                selectedWeekdaysSet = parsedSet

                // Interval days
                customIntervalDays = task.customIntervalDays?.toString() ?: "3"
            }
        }
    }

    var recurrenceDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) trans("edit_task") else trans("add_task"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
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
            // 1. عنوان کار (Title) - REQUIRED
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    label = { 
                        Text(if (lang == "fa") "عنوان یادآور (الزامی)" else "Reminder Title (Required)") 
                    },
                    placeholder = { 
                        Text(if (lang == "fa") "مثلاً: خرید دارو از داروخانه" else "e.g., Buy medicine from pharmacy") 
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    isError = titleError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                if (titleError != null) {
                    Text(
                        text = titleError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            // 2. توضیحات (Description)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { 
                    Text(if (lang == "fa") "توضیحات تکمیلی (اختیاری)" else "Additional Description (Optional)") 
                },
                placeholder = { 
                    Text(if (lang == "fa") "مثلاً: دوز مصرفی دارو ۲ قرص بعد از شام است..." else "e.g., Take 2 doses of the medicine after dinner...") 
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_description"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // 3. دسته‌بندی اختیاری (Category)
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { 
                    Text(if (lang == "fa") "دسته‌بندی اختیاری (اختیاری)" else "Optional Category") 
                },
                placeholder = { 
                    Text(if (lang == "fa") "مثلاً: سلامتی، خرید، کار، شخصی" else "e.g., Health, Shopping, Work, Personal") 
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Category, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_category"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // 4. تاریخ و ساعت یادآوری (Due Date & Time)
            val displayDateText = if (useGregorian) {
                dueDate
            } else {
                if (lang == "fa") {
                    JalaliCalendarHelper.englishToPersianDigits(
                        JalaliCalendarHelper.gregorianStrToJalaliStr(dueDate)
                    )
                } else {
                    JalaliCalendarHelper.gregorianStrToJalaliStr(dueDate)
                }
            }
            val displayTimeText = if (useGregorian || lang == "en") {
                dueTime
            } else {
                JalaliCalendarHelper.englishToPersianDigits(dueTime)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Due Date
                Column(modifier = Modifier.weight(1.1f)) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = displayDateText,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(if (useGregorian) trans("date") else trans("date_solar")) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                            },
                            isError = dateError != null,
                            modifier = Modifier.fillMaxWidth().testTag("input_due_date"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (dateError != null) {
                        Text(
                            text = dateError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                // Due Time
                Column(modifier = Modifier.weight(0.9f)) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                    ) {
                        OutlinedTextField(
                            value = displayTimeText,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(trans("reminder_time")) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                            },
                            isError = timeError != null,
                            modifier = Modifier.fillMaxWidth().testTag("input_due_time"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (timeError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    if (timeError != null) {
                        Text(
                            text = timeError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }

            if (showDatePicker) {
                CustomDatePickerDialog(
                    initialDateGregorian = dueDate,
                    useGregorian = useGregorian,
                    onDismissRequest = { showDatePicker = false },
                    onDateSelected = { selectedDate ->
                        dueDate = selectedDate
                        dateError = null
                        showDatePicker = false
                    }
                )
            }

            if (showTimePicker) {
                CustomTimePickerDialog(
                    initialTime = dueTime,
                    useGregorian = useGregorian,
                    onDismissRequest = { showTimePicker = false },
                    onTimeSelected = { selectedTime ->
                        dueTime = selectedTime
                        timeError = null
                        showTimePicker = false
                    }
                )
            }

            // Get translation lookup for Recurrence enum
            val currentSelectedRecurrenceTitle = when (recurrenceType) {
                RecurrenceType.ONE_TIME -> trans("recurrence_none")
                RecurrenceType.DAILY -> trans("recurrence_daily")
                RecurrenceType.WEEKLY -> trans("recurrence_weekly")
                RecurrenceType.MONTHLY -> trans("recurrence_monthly")
                RecurrenceType.YEARLY -> if (lang == "fa") "هر سال" else "Yearly"
                RecurrenceType.CUSTOM_WEEKDAYS -> trans("recurrence_weekdays")
                RecurrenceType.EVERY_X_DAYS -> trans("recurrence_custom")
            }

            // 5. نوع تکرار (Recurrence Type)
            ExposedDropdownMenuBox(
                expanded = recurrenceDropdownExpanded,
                onExpandedChange = { recurrenceDropdownExpanded = !recurrenceDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = currentSelectedRecurrenceTitle,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(trans("recurrence_type")) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Loop, contentDescription = null)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("recurrence_dropdown"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = recurrenceDropdownExpanded,
                    onDismissRequest = { recurrenceDropdownExpanded = false }
                ) {
                    RecurrenceType.values().forEach { type ->
                        val itemLabel = when (type) {
                            RecurrenceType.ONE_TIME -> trans("recurrence_none")
                            RecurrenceType.DAILY -> trans("recurrence_daily")
                            RecurrenceType.WEEKLY -> trans("recurrence_weekly")
                            RecurrenceType.MONTHLY -> trans("recurrence_monthly")
                            RecurrenceType.YEARLY -> if (lang == "fa") "هر سال" else "Yearly"
                            RecurrenceType.CUSTOM_WEEKDAYS -> trans("recurrence_weekdays")
                            RecurrenceType.EVERY_X_DAYS -> trans("recurrence_custom")
                        }
                        DropdownMenuItem(
                            text = { Text(text = itemLabel) },
                            onClick = {
                                recurrenceType = type
                                recurrenceDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            // 5a. Conditional weekdays row (روزهای هفته) - only when CUSTOM_WEEKDAYS
            AnimatedVisibility(
                visible = recurrenceType == RecurrenceType.CUSTOM_WEEKDAYS,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == "fa") "روزهای خاص هفته:" else "Specific weekdays:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val weekdaysName = if (lang == "fa") {
                            listOf("شنبه", "۱شنبه", "۲شنبه", "۳شنبه", "۴شنبه", "۵شنبه", "جمعه")
                        } else {
                            listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekdaysName.forEachIndexed { index, name ->
                                val isSelected = selectedWeekdaysSet.contains(index)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedWeekdaysSet = if (isSelected) {
                                                selectedWeekdaysSet - index
                                            } else {
                                                selectedWeekdaysSet + index
                                            }
                                            if (selectedWeekdaysSet.isNotEmpty()) {
                                                weekdaysError = null
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(2),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                        if (weekdaysError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = weekdaysError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 5b. Conditional interval day number (فاصله تکرار) - only when EVERY_X_DAYS
            AnimatedVisibility(
                visible = recurrenceType == RecurrenceType.EVERY_X_DAYS,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = customIntervalDays,
                            onValueChange = { 
                                customIntervalDays = it
                                customIntervalError = null
                            },
                            label = { Text(if (lang == "fa") "فاصله تکرار (تعداد روز)" else "Recurrence interval (Days)") },
                            placeholder = { Text(if (lang == "fa") "مثلاً: ۳ برای هر ۳ روز یکبار" else "e.g., 3 for every 3 days") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_interval_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            isError = customIntervalError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (customIntervalError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = customIntervalError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 6. زمان تعویق یادآوری (Snooze selection)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "fa") "زمان تعویق یادآوری مجدد:" else "Reminder Snooze Interval:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val snoozeOptionsList = if (lang == "fa") {
                        listOf("۵ دقیقه", "۱۰ دقیقه", "سفارشی")
                    } else {
                        listOf("5 Mins", "10 Mins", "Custom")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        snoozeOptionsList.forEach { option ->
                            val isSelected = snoozeOption == option
                            FilterChip(
                                selected = isSelected,
                                onClick = { snoozeOption = option },
                                label = { Text(option) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Show textfield only if "سفارشی" / "Custom" is chosen
                    AnimatedVisibility(
                        visible = (snoozeOption == "سفارشی" || snoozeOption == "Custom"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customSnoozeMinutes,
                                onValueChange = { customSnoozeMinutes = it },
                                label = { Text(if (lang == "fa") "مدت زمان یادآوری بر حسب دقیقه" else "Reminder duration in minutes") },
                                placeholder = { Text(if (lang == "fa") "مثلاً ۱۵" else "e.g., 15") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Snooze, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_snooze"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save/Submit Button with thorough validations
            Button(
                onClick = {
                    var hasError = false
                    
                    // Validation 1: Title required
                    if (title.isBlank()) {
                        titleError = if (lang == "fa") "عنوان یادآور الزامی است." else "Reminder title is required."
                        hasError = true
                    } else {
                        titleError = null
                    }

                    // Validation 2: Due Date required
                    if (dueDate.isBlank()) {
                        dateError = if (lang == "fa") "تاریخ یادآوری الزامی است." else "Reminder date is required."
                        hasError = true
                    } else {
                        val dateRegex = """^\d{4}-\d{2}-\d{2}$""".toRegex()
                        if (!dueDate.matches(dateRegex)) {
                            dateError = if (lang == "fa") "فرمت تاریخ نامعتبر است (مانند: 2026-06-18)" else "Invalid date format (e.g., 2026-06-18)"
                            hasError = true
                        } else {
                            dateError = null
                        }
                    }

                    // Validation 3: Due Time required
                    if (dueTime.isBlank()) {
                        timeError = if (lang == "fa") "ساعت یادآوری الزامی است." else "Reminder time is required."
                        hasError = true
                    } else {
                        val timeRegex = """^\d{2}:\d{2}$""".toRegex()
                        if (!dueTime.matches(timeRegex)) {
                            timeError = if (lang == "fa") "فرمت ساعت نامعتبر است (مانند: 14:15)" else "Invalid time format (e.g., 14:15)"
                            hasError = true
                        } else {
                            timeError = null
                        }
                    }

                    // Validation 4: Custom intervals
                    val intervalVal = if (recurrenceType == RecurrenceType.EVERY_X_DAYS) {
                        val parsed = customIntervalDays.toIntOrNull()
                        if (parsed == null || parsed <= 0) {
                            customIntervalError = if (lang == "fa") "فاصله تکرار باید بزرگتر از صفر باشد." else "Interval must be greater than zero."
                            hasError = true
                        } else {
                            customIntervalError = null
                        }
                        parsed
                    } else null

                    // Validation 5: Weekdays selection
                    val weekdaysString = if (recurrenceType == RecurrenceType.CUSTOM_WEEKDAYS) {
                        if (selectedWeekdaysSet.isEmpty()) {
                            weekdaysError = if (lang == "fa") "حداقل یکی از روزهای هفته باید انتخاب شود." else "At least one weekday must be selected."
                            hasError = true
                        } else {
                            weekdaysError = null
                        }
                        selectedWeekdaysSet.joinToString(",")
                    } else null

                    if (hasError) {
                        scope.launch {
                            snackbarHostState.showSnackbar(if (lang == "fa") "لطفاً خطاهای فرم را برطرف نمایید." else "Please fix the form errors.")
                        }
                        return@Button
                    }

                    // Parse snooze minutes based on the chosen option
                    val finalSnoozeVal = when (snoozeOption) {
                        "۵ دقیقه", "5 Mins" -> 5
                        "۱۰ دقیقه", "10 Mins" -> 10
                        else -> customSnoozeMinutes.toIntOrNull() ?: 10
                    }

                    val onSuccessCallback = {
                        onNavigateBack()
                    }
                    val onFailureCallback: (String) -> Unit = { errorMsg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(errorMsg)
                        }
                    }

                    val normalizedTitle = title.trim()
                    val normalizedDesc = description.trim()
                    val normalizedCat = category.trim()

                    if (isEditMode && originalTask != null) {
                        val updatedTask = originalTask!!.copy(
                            title = normalizedTitle,
                            description = normalizedDesc,
                            category = if (normalizedCat.isBlank()) null else normalizedCat,
                            recurrenceType = recurrenceType,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            selectedWeekdays = weekdaysString,
                            customIntervalDays = intervalVal,
                            snoozeMinutes = finalSnoozeVal
                        )
                        viewModel.updateTask(
                            task = updatedTask,
                            onSuccess = onSuccessCallback,
                            onFailure = onFailureCallback
                        )
                    } else {
                        viewModel.addTask(
                            title = normalizedTitle,
                            description = normalizedDesc,
                            category = if (normalizedCat.isBlank()) null else normalizedCat,
                            recurrenceType = recurrenceType,
                            dueDate = dueDate,
                            dueTime = dueTime,
                            selectedWeekdays = weekdaysString,
                            customIntervalDays = intervalVal,
                            snoozeMinutes = finalSnoozeVal,
                            onSuccess = onSuccessCallback,
                            onFailure = onFailureCallback
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditMode) trans("save_changes") else trans("create_reminder"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
