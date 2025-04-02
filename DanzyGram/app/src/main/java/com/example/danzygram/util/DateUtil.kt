package com.example.danzygram.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtil {
    private val locale = Locale.getDefault()
    private val utcTimeZone = TimeZone.getTimeZone("UTC")

    private val fullDateFormat = SimpleDateFormat("MMMM dd, yyyy", locale)
    private val shortDateFormat = SimpleDateFormat("MMM dd, yyyy", locale)
    private val timeFormat = SimpleDateFormat("h:mm a", locale)
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", locale)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", locale).apply {
        timeZone = utcTimeZone
    }

    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diff = now.time - date.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            days < 30 -> "${days / 7}w"
            days < 365 -> "${days / 30}mo"
            else -> "${days / 365}y"
        }
    }

    fun formatFullDate(date: Date): String {
        return fullDateFormat.format(date)
    }

    fun formatShortDate(date: Date): String {
        return shortDateFormat.format(date)
    }

    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }

    fun formatDateTime(date: Date): String {
        return dateTimeFormat.format(date)
    }

    fun formatISODate(date: Date): String {
        return isoFormat.format(date)
    }

    fun parseISODate(dateString: String): Date? {
        return try {
            isoFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getStartOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    fun getStartOfWeek(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfWeek(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek + 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    fun getStartOfMonth(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfMonth(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    fun addDays(date: Date, days: Int): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, days)
        }.time
    }

    fun addMonths(date: Date, months: Int): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.MONTH, months)
        }.time
    }

    fun addYears(date: Date, years: Int): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.YEAR, years)
        }.time
    }

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isSameWeek(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    fun getDaysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun getMonthsBetween(startDate: Date, endDate: Date): Int {
        val cal1 = Calendar.getInstance().apply { time = startDate }
        val cal2 = Calendar.getInstance().apply { time = endDate }
        val yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)
        return yearDiff * 12 + cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH)
    }
}