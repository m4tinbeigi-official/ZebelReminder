package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.JalaliCalendarHelper
import java.util.Calendar
import java.util.GregorianCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    initialDateGregorian: String, // format: "yyyy-MM-dd"
    useGregorian: Boolean,
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    // Parse the initial date
    var initialYearPref = 2026
    var initialMonthPref = 6
    var initialDayPref = 18

    try {
        val parts = initialDateGregorian.split("-")
        if (parts.size >= 3) {
            initialYearPref = parts[0].toInt()
            initialMonthPref = parts[1].toInt()
            initialDayPref = parts[2].toInt()
        }
    } catch (e: Exception) {
        val cal = Calendar.getInstance()
        initialYearPref = cal.get(Calendar.YEAR)
        initialMonthPref = cal.get(Calendar.MONTH) + 1
        initialDayPref = cal.get(Calendar.DAY_OF_MONTH)
    }

    // Convert to jalali if needed
    val initialJalali = JalaliCalendarHelper.gregorianToJalali(initialYearPref, initialMonthPref, initialDayPref)

    // Browsing fields
    var viewingYear by remember {
        mutableStateOf(if (useGregorian) initialYearPref else initialJalali.year)
    }
    var viewingMonth by remember {
        mutableStateOf(if (useGregorian) initialMonthPref else initialJalali.month) // 1-indexed
    }
    var selectedYear by remember {
        mutableStateOf(if (useGregorian) initialYearPref else initialJalali.year)
    }
    var selectedMonth by remember {
        mutableStateOf(if (useGregorian) initialMonthPref else initialJalali.month)
    }
    var selectedDay by remember {
        mutableStateOf(if (useGregorian) initialDayPref else initialJalali.day)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Persian)
                Text(
                    text = if (useGregorian) "انتخاب تاریخ میلادی" else "انتخاب تاریخ خورشیدی",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Traversal row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (viewingMonth == 12) {
                                viewingMonth = 1
                                viewingYear++
                            } else {
                                viewingMonth++
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "ماه بعد"
                        )
                    }

                    // Month and Year label
                    val monthName = if (useGregorian) {
                        getGregorianMonthName(viewingMonth)
                    } else {
                        getJalaliMonthName(viewingMonth)
                    }
                    val formattedLabel = if (useGregorian) {
                        "$monthName $viewingYear"
                    } else {
                        val labelStr = "$monthName $viewingYear"
                        JalaliCalendarHelper.englishToPersianDigits(labelStr)
                    }

                    Text(
                        text = formattedLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            if (viewingMonth == 1) {
                                viewingMonth = 12
                                viewingYear--
                            } else {
                                viewingMonth--
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "ماه قبل"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekday headers: ش، ی، د، س، چ، پ، ج (Saturday first layout)
                val weekdayTitles = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekdayTitles.forEach { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calculate month stats
                val daysInMonth = if (useGregorian) {
                    val gCal = GregorianCalendar(viewingYear, viewingMonth - 1, 1)
                    gCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                } else {
                    if (viewingMonth in 1..6) 31
                    else if (viewingMonth in 7..11) 30
                    else if (JalaliCalendarHelper.isJalaliLeapYear(viewingYear)) 30 else 29
                }

                val offset = if (useGregorian) {
                    val gCal = GregorianCalendar(viewingYear, viewingMonth - 1, 1)
                    getWeekdayIndex(gCal.get(Calendar.DAY_OF_WEEK))
                } else {
                    // Convert Jalali (Year, Month, 1) to Gregorian to identify day of week offset
                    val gregDate = JalaliCalendarHelper.jalaliToGregorian(viewingYear, viewingMonth, 1)
                    val gCal = GregorianCalendar(gregDate.year, gregDate.month - 1, gregDate.day)
                    getWeekdayIndex(gCal.get(Calendar.DAY_OF_WEEK))
                }

                // Days Grid (6 rows x 7 days)
                val totalCells = 42
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    items(totalCells) { index ->
                        val dayNumber = index - offset + 1
                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = selectedYear == viewingYear &&
                                    selectedMonth == viewingMonth &&
                                    selectedDay == dayNumber

                            val cellText = if (useGregorian) dayNumber.toString() else JalaliCalendarHelper.englishToPersianDigits(dayNumber.toString())

                            Box(
                                modifier = Modifier
                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        selectedYear = viewingYear
                                        selectedMonth = viewingMonth
                                        selectedDay = dayNumber
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected indicator
                val currentSelectedText = if (useGregorian) {
                    "انتخاب‌شده: $selectedYear-$selectedMonth-$selectedDay"
                } else {
                    val formatted = "انتخاب‌شده: $selectedYear/$selectedMonth/$selectedDay"
                    JalaliCalendarHelper.englishToPersianDigits(formatted)
                }

                Text(
                    text = currentSelectedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("انصراف", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalDateGregorian = if (useGregorian) {
                                String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                            } else {
                                JalaliCalendarHelper.jalaliToGregorian(selectedYear, selectedMonth, selectedDay).toString()
                            }
                            onDateSelected(finalDateGregorian)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تایید", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomTimePickerDialog(
    initialTime: String, // "HH:mm"
    useGregorian: Boolean,
    onDismissRequest: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    var initialHour = 9
    var initialMinute = 0
    try {
        val parts = initialTime.split(":")
        if (parts.size >= 2) {
            initialHour = parts[0].toInt()
            initialMinute = parts[1].toInt()
        }
    } catch (e: Exception) {
        // use default
    }

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    var activeField by remember { mutableStateOf(0) } // 0 = hour, 1 = minute

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتخاب زمان بارگذاری",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Large Digital Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Minute
                    val displayMin = String.format("%02d", selectedMinute)
                    Box(
                        modifier = Modifier
                            .background(
                                if (activeField == 1) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (activeField == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeField = 1 }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (useGregorian) displayMin else JalaliCalendarHelper.englishToPersianDigits(displayMin),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (activeField == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Text(
                        text = " : ",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Hour
                    val displayHour = String.format("%02d", selectedHour)
                    Box(
                        modifier = Modifier
                            .background(
                                if (activeField == 0) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = if (activeField == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeField = 0 }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = if (useGregorian) displayHour else JalaliCalendarHelper.englishToPersianDigits(displayHour),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (activeField == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (activeField == 0) {
                    // Grid of hours (0..23)
                    Text("انتخاب ساعت", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(24) { hour ->
                            val isSelected = selectedHour == hour
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        selectedHour = hour
                                        activeField = 1 // Auto advance to select minutes
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val hrStr = String.format("%02d", hour)
                                Text(
                                    text = if (useGregorian) hrStr else JalaliCalendarHelper.englishToPersianDigits(hrStr),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    // Grid of minutes (multiples of 5 or keyboard adjust)
                    Text("انتخاب دقیقه", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    val minuteOptions = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(minuteOptions.size) { index ->
                            val minute = minuteOptions[index]
                            val isSelected = selectedMinute == minute
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        selectedMinute = minute
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val minStr = String.format("%02d", minute)
                                Text(
                                    text = if (useGregorian) minStr else JalaliCalendarHelper.englishToPersianDigits(minStr),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Minute manual adj row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (selectedMinute < 59) selectedMinute++
                                else selectedMinute = 0
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("+۱ دقبقه")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                if (selectedMinute > 0) selectedMinute--
                                else selectedMinute = 59
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("-۱ دقیقه")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("انصراف", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                            onTimeSelected(finalTime)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تایید", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

// Helpers
private fun getWeekdayIndex(calendarDayOfWeek: Int): Int {
    // Saturday is start of week in Persian calendar
    return when (calendarDayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }
}

private fun getJalaliMonthName(month: Int): String {
    return when (month) {
        1 -> "فروردین"
        2 -> "اردیبهشت"
        3 -> "خرداد"
        4 -> "تیر"
        5 -> "مرداد"
        6 -> "شهریور"
        7 -> "مهر"
        8 -> "آبان"
        9 -> "آذر"
        10 -> "دی"
        11 -> "بهمن"
        12 -> "اسفند"
        else -> ""
    }
}

private fun getGregorianMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan (Jan)"
        2 -> "Feb (Feb)"
        3 -> "Mar (Mar)"
        4 -> "Apr (Apr)"
        5 -> "May (May)"
        6 -> "Jun (Jun)"
        7 -> "Jul (Jul)"
        8 -> "Aug (Aug)"
        9 -> "Sep (Sep)"
        10 -> "Oct (Oct)"
        11 -> "Nov (Nov)"
        12 -> "Dec (Dec)"
        else -> ""
    }
}
