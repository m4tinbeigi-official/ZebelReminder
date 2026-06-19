package com.example.util

object JalaliCalendarHelper {
    class JalaliDate(val year: Int, val month: Int, val day: Int) {
        override fun toString() = "$year/${String.format("%02d", month)}/${String.format("%02d", day)}"
        fun toDashString() = "$year-${String.format("%02d", month)}-${String.format("%02d", day)}"
        
        fun getMonthName(): String {
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
    }

    class GregorianDate(val year: Int, val month: Int, val day: Int) {
        override fun toString() = "$year-${String.format("%02d", month)}-${String.format("%02d", day)}"
    }

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val gy = gYear - 1600
        val gm = gMonth
        val gd = gDay

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 1 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 2 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd - 1

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 1..12) {
            val days = if (i == 12 && isJalaliLeapYear(jy)) 30 else jDaysInMonth[i]
            if (jDayNo < days) {
                jm = i
                break
            }
            jDayNo -= days
        }
        val jd = jDayNo + 1
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): GregorianDate {
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        val jYearOffset = jy - 979
        var jDayNo = 365 * jYearOffset + (jYearOffset / 33) * 8 + (jYearOffset % 33 + 3) / 4
        for (i in 1 until jm) {
            jDayNo += jDaysInMonth[i]
        }
        jDayNo += jd - 1

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        var gm = 0
        for (i in 1..12) {
            val days = if (i == 2 && leap) 29 else gDaysInMonth[i]
            if (gDayNo < days) {
                gm = i
                break
            }
            gDayNo -= days
        }
        val gd = gDayNo + 1
        return GregorianDate(gy, gm, gd)
    }

    fun isJalaliLeapYear(year: Int): Boolean {
        val r = year % 33
        return r == 1 || r == 5 || r == 9 || r == 13 || r == 17 || r == 22 || r == 26 || r == 30
    }

    /**
     * Converts a dash separated YYYY-MM-DD Gregorian date to a formatted Shamsi date (e.g., 1405/03/28)
     */
    fun gregorianStrToJalaliStr(gDateStr: String): String {
        return try {
            val parts = gDateStr.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val jalali = gregorianToJalali(y, m, d)
            jalali.toString()
        } catch (e: Exception) {
            gDateStr
        }
    }

    /**
     * Converts a slash separated yyyy/MM/dd Shamsi date to a Gregorian yyyy-MM-dd dash separated string
     */
    fun jalaliStrToGregorianStr(jDateStr: String): String {
        return try {
            val parts = jDateStr.replace("-", "/").split("/")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val greg = jalaliToGregorian(y, m, d)
            greg.toString()
        } catch (e: Exception) {
            jDateStr
        }
    }

    /**
     * Converts Persian numbers to English digits for parser stability
     */
    fun persianToEnglishDigits(input: String): String {
        var result = input
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in 0..9) {
            result = result.replace(persianDigits[i], i.toString()[0])
            result = result.replace(arabicDigits[i], i.toString()[0])
        }
        return result
    }

    /**
     * Converts English numbers to Persian digits for visual/RTL friendly display
     */
    fun englishToPersianDigits(input: String): String {
        var result = ""
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        for (element in input) {
            if (element in '0'..'9') {
                result += persianDigits[element - '0']
            } else {
                result += element
            }
        }
        return result
    }
}
