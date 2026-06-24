package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val viLocale = Locale("vi", "VN")

    fun formatDateVietnamese(millis: Long): String {
        val sdf = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", viLocale)
        val result = sdf.format(Date(millis))
        return result.replaceFirstChar { if (it.isLowerCase()) it.titlecase(viLocale) else it.toString() }
    }

    fun formatDateShort(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM", viLocale)
        return sdf.format(Date(millis))
    }

    fun getDayOfWeekAbbreviated(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "CN"
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            else -> ""
        }
    }

    fun getDayOfMonth(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        return cal.get(Calendar.DAY_OF_MONTH).toString()
    }

    fun getWeekDays(centerMillis: Long): List<Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = centerMillis
        
        // Find Monday of the current week
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        
        // Set to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val days = mutableListOf<Long>()
        for (i in 0 until 14) { // Generate 2 weeks of scroll to give extra calendar span!
            days.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }

    fun getMonthDays(centerMillis: Long): List<Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = centerMillis
        val currentMonth = cal.get(Calendar.MONTH)
        
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
        
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val days = mutableListOf<Long>()
        // Generate first 35 days (5 weeks)
        for (i in 0 until 35) {
            days.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        // Check if we need a 6th week
        val tempCal = cal.clone() as Calendar
        var needsSixthWeek = false
        for (i in 0 until 7) {
            if (tempCal.get(Calendar.MONTH) == currentMonth) {
                needsSixthWeek = true
                break
            }
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        if (needsSixthWeek) {
            for (i in 0 until 7) {
                days.add(cal.timeInMillis)
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return days
    }

    fun formatMonthYearVietnamese(millis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        return "Tháng $month, $year"
    }
}
