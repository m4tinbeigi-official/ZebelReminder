package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineSuggestion
import com.example.ui.components.*
import com.example.ui.viewmodel.ProgressViewModel
import com.example.util.DateUtils
import com.example.util.JalaliCalendarHelper
import com.example.util.LocalLanguage
import com.example.util.Localization
import com.example.util.RoutineSuggestionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDashboardScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = LocalLanguage.current
    val trans = { key: String -> Localization.getString(key, lang) }

    val sharedPrefs = remember {
        context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
    }
    val useGregorian = sharedPrefs.getBoolean("use_gregorian_calendar", false)

    val todayDate = remember { DateUtils.getTodayDateString() }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyProgress by viewModel.dailyProgress.collectAsState()
    val allProgressRecords by viewModel.allProgress.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Solar vs Gregorian header date representation
    val formattedSelectedDate = remember(selectedDate, useGregorian, lang) {
        if (useGregorian) {
            DateUtils.getFriendlyDateLabel(selectedDate)
        } else {
            val jalali = JalaliCalendarHelper.gregorianStrToJalaliStr(selectedDate)
            if (lang == "fa") JalaliCalendarHelper.englishToPersianDigits(jalali) else jalali
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (lang == "fa") "گزارش پیشرفت" else "Progress Progress Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("progress_back_button")
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
            // Check if there are no routine progress items or routine tasks created at all (to display Persian Empty States)
            val hasData = uiState.todayTotalCount > 0 || uiState.weekTotalCount > 0 || uiState.monthTotalCount > 0 || allProgressRecords.isNotEmpty()

            if (!hasData) {
                // Empty State UI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = if (lang == "fa") "هنوز داده‌ای برای نمایش پیشرفت وجود ندارد." else "No progress data available yet.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == "fa") "چند روتین انجام بده تا گزارش پیشرفتت ساخته شود." else "Complete some routines to generate your progress report.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Calendar selector Row & Reset button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (lang == "fa") "خلاصه وضعیت روتین‌ها" else "Routine Status Summary",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedSelectedDate,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.clearAllProgress()
                                Toast.makeText(context, if (lang == "fa") "آمار شروع مجدد شد" else "Progress stats reset", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(trans("reset_task_or_refresh"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Summary cards (Today, Weekly, Monthly progress)
                Text(
                    text = if (lang == "fa") "آمارهای دوره‌ای" else "Periodic Stats",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProgressSummaryCard(
                        title = if (lang == "fa") "امروز" else "Today",
                        completedCount = uiState.todayCompletedCount,
                        totalCount = uiState.todayTotalCount,
                        percentage = uiState.todayPercentage,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        lang = lang
                    )
                    ProgressSummaryCard(
                        title = if (lang == "fa") "این هفته" else "Weekly",
                        completedCount = uiState.weekCompletedCount,
                        totalCount = uiState.weekTotalCount,
                        percentage = uiState.weekPercentage,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        lang = lang
                    )
                    ProgressSummaryCard(
                        title = if (lang == "fa") "این ماه" else "Monthly",
                        completedCount = uiState.monthCompletedCount,
                        totalCount = uiState.monthTotalCount,
                        percentage = uiState.monthPercentage,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        lang = lang
                    )
                }

                // 3. Streak section
                StreakCard(
                    currentStreak = uiState.currentDailyStreak,
                    bestStreak = uiState.bestDailyStreak,
                    lang = lang
                )

                // 4. Motivational Progress Message based on daily completion percentage
                MotivationalProgressMessage(
                    percentage = uiState.todayPercentage,
                    lang = lang
                )

                // 5. Visual Progress Charts: Daily, Weekly, and Monthly
                Text(
                    text = if (lang == "fa") "نمودارهای تحلیل پایبندی" else "Compliance Analysis Charts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                SimpleBarChart(
                    data = uiState.dailyChartData,
                    title = if (lang == "fa") "پیشرفت روزانه روتین‌ها (۷ روز اخیر)" else "Daily Routine Progress (Last 7 Days)",
                    color = MaterialTheme.colorScheme.primary,
                    lang = lang
                )

                SimpleBarChart(
                    data = uiState.weeklyChartData,
                    title = if (lang == "fa") "پیشرفت هفتگی روتین‌ها (۴ هفته اخیر)" else "Weekly Routine Progress (Last 4 Weeks)",
                    color = MaterialTheme.colorScheme.secondary,
                    lang = lang
                )

                SimpleBarChart(
                    data = uiState.monthlyChartData,
                    title = if (lang == "fa") "پیشرفت ماهانه روتین‌ها (۶ ماه اخیر)" else "Monthly Routine Progress (Last 6 Months)",
                    color = MaterialTheme.colorScheme.tertiary,
                    lang = lang
                )

                // 6. Category progress
                CategoryProgressList(
                    categoryProgressItems = uiState.categoryProgressList,
                    lang = lang
                )
            }

            // 5. Routine suggestions list
            Text(
                text = if (lang == "fa") "عادت‌های خودمراقبتی و ثبت فوری" else "Self-Care Habits & Direct Log",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            viewModel.suggestions.forEach { suggestion ->
                val progressRecord = dailyProgress.find { it.routineId == suggestion.id }
                val isCompleted = progressRecord?.isCompleted ?: false

                DailyRoutineCard(
                    suggestion = suggestion,
                    isCompleted = isCompleted,
                    progressRecord = progressRecord,
                    lang = lang,
                    onToggle = {
                        viewModel.toggleRoutineCompletion(suggestion, selectedDate, progressRecord)
                    },
                    onIncrement = {
                        viewModel.incrementRoutineValue(suggestion, selectedDate, progressRecord, targetValue = 8)
                    }
                )
            }
        }
    }
}

@Composable
fun StatProgressCard(
    title: String,
    completed: Int,
    total: Int,
    percentage: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    lang: String
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage.toFloat() / 100f },
                    modifier = Modifier.size(50.dp),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f),
                    strokeWidth = 6.dp
                )
                val friendlyPercent = if (lang == "fa") {
                    JalaliCalendarHelper.englishToPersianDigits("$percentage%")
                } else {
                    "$percentage%"
                }
                Text(
                    text = friendlyPercent,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            val friendlyFraction = if (lang == "fa") {
                JalaliCalendarHelper.englishToPersianDigits("$completed/$total")
            } else {
                "$completed/$total"
            }
            Text(
                text = friendlyFraction,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryProgressBar(
    category: String,
    completed: Int,
    total: Int,
    percentage: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            val friendlyDetails = if (lang == "fa") {
                JalaliCalendarHelper.englishToPersianDigits("$completed از $total ($percentage٪)")
            } else {
                "$completed of $total ($percentage%)"
            }
            Text(
                text = friendlyDetails,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        LinearProgressIndicator(
            progress = { percentage.toFloat() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun DailyRoutineCard(
    suggestion: RoutineSuggestion,
    isCompleted: Boolean,
    progressRecord: com.example.data.model.RoutineProgressEntity?,
    lang: String,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleStr = RoutineSuggestionProvider.getLocalizedTitle(suggestion, lang)
    val descStr = RoutineSuggestionProvider.getLocalizedDesc(suggestion, lang)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCompleted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val vIcon = when (suggestion.iconName) {
                            "water" -> Icons.Default.WaterDrop
                            "walk" -> Icons.Default.DirectionsWalk
                            "book" -> Icons.Default.MenuBook
                            "mind" -> Icons.Default.SelfImprovement
                            "sleep" -> Icons.Default.Bedtime
                            else -> Icons.Default.FitnessCenter
                        }
                        Icon(
                            imageVector = vIcon,
                            contentDescription = null,
                            tint = if (isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = titleStr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                             )
                            Spacer(modifier = Modifier.width(4.dp))
                            val friendlyTime = if (lang == "fa") {
                                JalaliCalendarHelper.englishToPersianDigits(suggestion.recommendedTime)
                            } else {
                                suggestion.recommendedTime
                            }
                            Text(
                                text = friendlyTime,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                // Toggle / Increment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (suggestion.targetType == "count") {
                        val currentVal = progressRecord?.progressValue ?: 0
                        val targetVal = progressRecord?.targetValue ?: 8
                        val friendlyProgressStr = if (lang == "fa") {
                            JalaliCalendarHelper.englishToPersianDigits("$currentVal / $targetVal")
                        } else {
                            "$currentVal / $targetVal"
                        }
                        Text(
                            text = friendlyProgressStr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(
                            onClick = onIncrement,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        IconButton(
                            onClick = onToggle,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isCompleted) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = descStr,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
