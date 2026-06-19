package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.JalaliCalendarHelper

@Composable
fun ProgressSummaryCard(
    title: String,
    completedCount: Int,
    totalCount: Int,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier,
    lang: String = "fa"
) {
    Card(
        modifier = modifier.height(135.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage.toFloat() / 100f },
                    modifier = Modifier.size(54.dp),
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
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                )
            }

            val friendlyFraction = if (lang == "fa") {
                JalaliCalendarHelper.englishToPersianDigits("$completedCount از $totalCount")
            } else {
                "$completedCount of $totalCount"
            }
            Text(
                text = friendlyFraction,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>, // List of Label to Completion Percentage (0 to 100)
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    lang: String = "fa"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty() || data.all { it.second == 0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "fa") "داده‌ای برای این نمودار وجود ندارد" else "No data for this chart",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEach { (label, percentage) ->
                        val animatedPercent by animateFloatAsState(
                            targetValue = percentage.coerceIn(0, 100) / 100f,
                            animationSpec = tween(durationMillis = 800),
                            label = "BarHeight"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            val displayPercent = if (lang == "fa") {
                                JalaliCalendarHelper.englishToPersianDigits("$percentage%")
                            } else {
                                "$percentage%"
                            }

                            Text(
                                text = displayPercent,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage > 0) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(18.dp)
                                    .weight(1f)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                    ),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedPercent.coerceAtLeast(0.02f))
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    currentStreak: Int,
    bestStreak: Int,
    modifier: Modifier = Modifier,
    lang: String = "fa"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == "fa") "زنجیره فعلی" else "Current Streak",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val currentStreakStr = if (lang == "fa") {
                    JalaliCalendarHelper.englishToPersianDigits("$currentStreak روز متوالی")
                } else {
                    "$currentStreak consecutive days"
                }
                Text(
                    text = currentStreakStr,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == "fa") "بهترین زنجیره" else "Best Streak",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val bestStreakStr = if (lang == "fa") {
                    JalaliCalendarHelper.englishToPersianDigits("$bestStreak روز متوالی")
                } else {
                    "$bestStreak consecutive days"
                }
                Text(
                    text = bestStreakStr,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        }
    }
}

@Composable
fun ProgressBarItem(
    category: String,
    completed: Int,
    total: Int,
    percentage: Int,
    lang: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        LinearProgressIndicator(
            progress = { percentage.toFloat() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun CategoryProgressList(
    categoryProgressItems: List<com.example.ui.viewmodel.CategoryProgress>,
    lang: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (lang == "fa") "پیشرفت دسته‌بندی‌ها" else "Category Progress",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            val activeCategories = categoryProgressItems.filter { it.totalCount > 0 }
            if (activeCategories.isEmpty()) {
                Text(
                    text = if (lang == "fa") {
                        "هنوز هیچ روتینی در دسته‌بندی‌ها ندارید. برای مشاهده پیشرفت تفکیکی، یادآور روتین ایجاد کنید."
                    } else {
                        "No active routines in categories. Create routine reminders to unlock category progress."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            } else {
                activeCategories.forEach { item ->
                    ProgressBarItem(
                        category = item.category,
                        completed = item.completedCount,
                        total = item.totalCount,
                        percentage = item.percentage,
                        lang = lang
                    )
                }
            }
        }
    }
}

@Composable
fun MotivationalProgressMessage(
    percentage: Int,
    modifier: Modifier = Modifier,
    lang: String = "fa"
) {
    val message = remember(percentage, lang) {
        if (lang == "fa") {
            when {
                percentage <= 30 -> "شروعش مهمه؛ ادامه بده."
                percentage <= 60 -> "خوبه! داری مسیرت رو میسازی."
                percentage <= 85 -> "عالیه! پیشرفتت کاملاً مشخصه."
                else -> "فوق‌العاده‌ای! همین روند رو حفظ کن."
            }
        } else {
            when {
                percentage <= 30 -> "The beginning is the hardest part; keep going!"
                percentage <= 60 -> "Nice work! You are building your path."
                percentage <= 85 -> "Awesome! Your progress is clearly visible."
                else -> "Incredible! Keep up this amazing momentum."
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Retro-compatibility helpers if needed by other components
@Composable
fun RoutineCompletionRing(
    percentage: Float,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 14.dp,
    centerLabel: String = "",
    lang: String = "fa"
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = strokeWidth
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val percentageText = (percentage * 100).toInt().toString()
            val formattedPercentage = if (lang == "fa") {
                JalaliCalendarHelper.englishToPersianDigits("$percentageText٪")
            } else {
                "$percentageText%"
            }
            Text(
                text = formattedPercentage,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (centerLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
