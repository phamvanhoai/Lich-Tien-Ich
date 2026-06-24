package com.example.utils

import android.icu.util.ChineseCalendar
import android.icu.util.Calendar

object LunarUtils {
    
    /**
     * Converts a solar date (given in milliseconds) into a Vietnamese Lunar date representation.
     * Returns a string like "15/05 Âm lịch"
     */
    fun getLunarDateString(millis: Long): String {
        val (day, month, isLeap) = getLunarDate(millis)
        val leapStr = if (isLeap) " (Nhuận)" else ""
        return "$day/$month$leapStr Âm lịch"
    }

    /**
     * Returns the Lunar day as a String, using traditional "Mùng" prefix for days 1 to 10
     */
    fun getLunarDayName(day: Int): String {
        return when (day) {
            in 1..10 -> "Mùng $day"
            else -> day.toString()
        }
    }

    /**
     * Converts solar date to Lunar Triple (Day, Month, IsLeap)
     */
    fun getLunarDate(millis: Long): Triple<Int, Int, Boolean> {
        val cc = ChineseCalendar()
        cc.timeInMillis = millis
        
        val lunarDay = cc.get(ChineseCalendar.DAY_OF_MONTH)
        val lunarMonth = cc.get(ChineseCalendar.MONTH) + 1
        val isLeap = cc.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        
        return Triple(lunarDay, lunarMonth, isLeap)
    }

    /**
     * Returns the Can Chi name for a year (e.g. "Bính Ngọ", "Giáp Thìn")
     */
    fun getCanChiYear(solarYear: Int): String {
        val cans = listOf("Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ")
        val chis = listOf("Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi")
        
        val can = cans[solarYear % 10]
        val chi = chis[solarYear % 12]
        
        return "$can $chi"
    }
}
